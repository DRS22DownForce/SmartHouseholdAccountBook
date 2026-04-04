package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.entity.User;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private UserRegistrationTxService userRegistrationTxService;

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

        userApplicationService.ensureUserExists();

        verify(userRegistrationTxService).insertNewUser(eq(cognitoSub), eq(email));
    }

    @Test
    void ensureUserExists_ユーザーが存在する場合は何もしない() {
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        userApplicationService.ensureUserExists();

        verify(userRegistrationTxService, never()).insertNewUser(any(), any());
    }

    @Test
    void ensureUserExists_cognito_sub重複は他スレッド登録済みとして握りつぶす() throws Exception {
        when(currentAuthProvider.getCurrentEmail()).thenReturn(email);
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUser));

        SQLException sqlEx = new SQLException(
                "Duplicate entry for key 'users.uk_users_cognito_sub'", "23000", 1062);
        doThrow(new DataIntegrityViolationException("dup", sqlEx))
                .when(userRegistrationTxService)
                .insertNewUser(eq(cognitoSub), eq(email));

        userApplicationService.ensureUserExists();

        verify(userRegistrationTxService).insertNewUser(eq(cognitoSub), eq(email));
        verify(userRepository, times(2)).findByCognitoSub(cognitoSub);//初回のユーザー存在チェックと、重複エラー後のユーザー存在チェックの2回
    }

    @Test
    void ensureUserExists_cognito_sub以外の制約違反は再スローする() throws Exception {
        when(currentAuthProvider.getCurrentEmail()).thenReturn(email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.empty());

        SQLException sqlEx = new SQLException("Duplicate entry for key 'other_index'", "23000", 1062);
        doThrow(new DataIntegrityViolationException("dup", sqlEx))
                .when(userRegistrationTxService)
                .insertNewUser(eq(cognitoSub), eq(email));

        assertThrows(DataIntegrityViolationException.class, () -> userApplicationService.ensureUserExists());
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

