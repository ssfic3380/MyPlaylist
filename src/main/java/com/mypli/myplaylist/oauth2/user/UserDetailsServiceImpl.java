package com.mypli.myplaylist.oauth2.user;

import com.mypli.myplaylist.domain.Member;
import com.mypli.myplaylist.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findBySocialId(username)
                .orElseThrow(() -> new UsernameNotFoundException("No Found Member: " + username));

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setMember(member);

        return userDetails;
    }
}