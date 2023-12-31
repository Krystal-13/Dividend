package com.zerobase.dividend.sevice;

import com.zerobase.dividend.exception.impl.AlreadyExistUserException;
import com.zerobase.dividend.exception.impl.IncorrectPasswordException;
import com.zerobase.dividend.exception.impl.NoUserException;
import com.zerobase.dividend.model.Auth;
import com.zerobase.dividend.model.MemberEntity;
import com.zerobase.dividend.persist.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return this.memberRepository.findByUsername(username)
                .orElseThrow(NoUserException::new);

    }

    public MemberEntity register(Auth.SignUp member) {

        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));

        return this.memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member) {

        MemberEntity user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(NoUserException::new);

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        return user;
    }

}
