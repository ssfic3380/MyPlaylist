package com.mypli.myplaylist.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class PlaylistDto {

    @NotNull
    private Long playlistId;

    @NotNull @Size(max = 128)
    private String playlistName;

    @Size(max = 255)
    private String playlistImg;
}