package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.entity.User;
import com.example.backend.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserApplicationServiceのテストクラス
 * 
 * アプリケーションサービスのユースケースをテストします。
 */
@ExtendWith(MockitoExtension.class)
public class UserApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentAuthProvider currentAuthProvider;

    @InjectMocks
    // テスト対象のオブジェクトにモックを注入する
    private UserApplicationService userApplicationService;

    private String cognitoSub;
    private String email;
    
    @BeforeEach
    public void setUp() {
        cognitoSub = "cognitoSub";
        email = "test@example.com";
        when(currentAuthProvider.getCurrentSub()).thenReturn(cognitoSub);
    }

    @Test
    void getUser_ユーザーが存在しない場合はUserNotFoundExceptionをスローする() {
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userApplicationService.getUser());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ensureUserExists_ユーザーが存在しない場合は新規作成する() {
        when(currentAuthProvider.getCurrentEmail()).thenReturn(email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.empty());
        User savedUser = new User(cognitoSub, email);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userApplicationService.ensureUserExists();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void ensureUserExists_ユーザーが存在する場合は何もしない() {
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        userApplicationService.ensureUserExists();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUser_ユーザーが存在する場合は既存ユーザーを返し保存しない() {
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        User result = userApplicationService.getUser();

        assertEquals(existingUser, result);
        verify(userRepository, times(1)).findByCognitoSub(cognitoSub);
        verify(userRepository, times(0)).save(any(User.class));
    }
}

