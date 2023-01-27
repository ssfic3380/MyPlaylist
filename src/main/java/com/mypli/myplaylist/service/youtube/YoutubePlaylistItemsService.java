package com.mypli.myplaylist.service.youtube;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.mypli.myplaylist.domain.Member;
import com.mypli.myplaylist.dto.youtube.YoutubePlaylistItemsDto;
import com.mypli.myplaylist.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubePlaylistItemsService {

    private final MemberRepository memberRepository;

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static YouTube youtube;

    public List<YoutubePlaylistItemsDto> get(String socialId, String playlistId) {

        List<YoutubePlaylistItemsDto> youtubePlaylistItemsDtoList = new ArrayList<>();

        try {

            Member member = memberRepository.findBySocialId(socialId);
            String accessToken = "";
            if(member != null) accessToken = member.getSocialAccessToken();
            else {
                log.error("Cannot find member from JwtAccessToken");
                return youtubePlaylistItemsDtoList;
            }

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("youtube-playlist-items-get").build();

            YouTube.PlaylistItems.List playlistItemsRequest = youtube.playlistItems().list("snippet");
            playlistItemsRequest.setPlaylistId(playlistId);
            playlistItemsRequest.setFields("nextPageToken," +
                    "items(kind,snippet/title,snippet/videoOwnerChannelTitle,snippet/resourceId/videoId,snippet/thumbnails/default/url)");

            String nextToken = "";
            List<PlaylistItem> playlistItemList = new ArrayList<>();
            do {
                playlistItemsRequest.setPageToken(nextToken);
                PlaylistItemListResponse playlistItemsResult = playlistItemsRequest.execute();

                playlistItemList.addAll(playlistItemsResult.getItems());

                nextToken = playlistItemsResult.getNextPageToken();
            } while (nextToken != null);

            if (playlistItemList != null) {
                makeDtoList(playlistItemList.iterator(), youtubePlaylistItemsDtoList);
            }

        } catch (GoogleJsonResponseException e) {
            log.error("유튜브 API 서비스 에러가 발생했습니다.", e);
        } catch (IOException e) {
            log.error("유튜브 API에서 IOException이 발생했습니다.", e);
        } catch (Throwable t) {
            log.error("유튜브 API에서 에러가 발생했습니다.", t);
        }

        return youtubePlaylistItemsDtoList;
    }

    private void makeDtoList(Iterator<PlaylistItem> iteratorSearchResults, List<YoutubePlaylistItemsDto> youtubePlaylistItemsDtoList) {

        if (!iteratorSearchResults.hasNext()) log.error("검색 결과가 없습니다.");

        while (iteratorSearchResults.hasNext()) {

            PlaylistItem singlePlaylistItem = iteratorSearchResults.next();

            log.info("singlePlaylistItem = {}", singlePlaylistItem);

            if (singlePlaylistItem.getKind().equals("youtube#playlistItem")) {
                String title = singlePlaylistItem.getSnippet().getTitle();
                String artist = (String) singlePlaylistItem.getSnippet().get("videoOwnerChannelTitle");
                String videoId = singlePlaylistItem.getSnippet().getResourceId().getVideoId();
                String thumbnail = singlePlaylistItem.getSnippet().getThumbnails().getDefault().getUrl();

                YoutubePlaylistItemsDto currentItem = YoutubePlaylistItemsDto.builder()
                        .title(title)
                        .artist(artist)
                        .videoId(videoId)
                        .thumbnail(thumbnail)
                        .build();

                youtubePlaylistItemsDtoList.add(currentItem);

            }
        }
    }

    /*public String get(String socialId, String playlistId, List<YoutubePlaylistItemsDto> youtubePlaylistItemsDtoList, String pageToken) {

        String nextPageToken = "";

        try {

            Member member = memberRepository.findBySocialId(socialId);
            String accessToken = "";
            if(member != null) accessToken = member.getSocialAccessToken();
            else {
                log.error("Cannot find member from JwtAccessToken");
                return "";
            }

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("youtube-playlist-items-get").build();

            YouTube.PlaylistItems.List playlistItems = youtube.playlistItems().list("snippet");
            playlistItems.setPlaylistId(playlistId);
            playlistItems.setPageToken(pageToken);
            playlistItems.setFields("nextPageToken," +
                    "items(kind,snippet/title,snippet/videoOwnerChannelTitle,snippet/resourceId/videoId,snippet/thumbnails/default/url)");

            PlaylistItemListResponse response = playlistItems.execute();
            List<PlaylistItem> playlistItemList = response.getItems();
            nextPageToken = response.getNextPageToken();

            if (playlistItemList != null) {
                makeDtoList(playlistItemList.iterator(), youtubePlaylistItemsDtoList);
            }

        } catch (GoogleJsonResponseException e) {
            log.error("유튜브 API 서비스 에러가 발생했습니다.", e);
        } catch (IOException e) {
            log.error("유튜브 API에서 IOException이 발생했습니다.", e);
        } catch (Throwable t) {
            log.error("유튜브 API에서 에러가 발생했습니다.", t);
        }

        return nextPageToken;
    }

    private void makeDtoList(Iterator<PlaylistItem> iteratorSearchResults, List<YoutubePlaylistItemsDto> youtubePlaylistItemsDtoList) {

        if (!iteratorSearchResults.hasNext()) log.error("검색 결과가 없습니다.");

        while (iteratorSearchResults.hasNext()) {

            PlaylistItem singlePlaylistItem = iteratorSearchResults.next();

            log.info("singlePlaylistItem = {}", singlePlaylistItem);

            if (singlePlaylistItem.getKind().equals("youtube#playlistItem")) {
                String title = singlePlaylistItem.getSnippet().getTitle();
                String artist = (String) singlePlaylistItem.getSnippet().get("videoOwnerChannelTitle");
                String videoId = singlePlaylistItem.getSnippet().getResourceId().getVideoId();
                String thumbnail = singlePlaylistItem.getSnippet().getThumbnails().getDefault().getUrl();

                YoutubePlaylistItemsDto currentItem = YoutubePlaylistItemsDto.builder()
                        .title(title)
                        .artist(artist)
                        .videoId(videoId)
                        .thumbnail(thumbnail)
                        .build();

                youtubePlaylistItemsDtoList.add(currentItem);

            }
        }
    }*/
}
