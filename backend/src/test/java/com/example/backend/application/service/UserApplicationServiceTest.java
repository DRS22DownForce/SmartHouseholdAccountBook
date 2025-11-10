package com.example.backend.application.service;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        when(currentAuthProvider.getCurrentEmail()).thenReturn(email);
    }

    @Test
    void createUserIfNotExists_ユーザーが存在しない場合はデータベースに保存() {
        // モックの設定: findByCognitoSubが空を返すように設定
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.empty());
        
        // モックの設定: saveが呼び出されたとき
        User savedUser = new User(cognitoSub, email);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // テスト実行: createUserIfNotExistsを呼び出す
        User result = userApplicationService.createUserIfNotExists();

        // 検証
        assertEquals(savedUser, result);
    }

    @Test
    void createUserIfNotExists_ユーザーが存在する場合はデータベースに保存しない() {
        // テストデータの準備
        User existingUser = new User(cognitoSub, email);
        
        // モックの設定
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        // テスト実行
        User result = userApplicationService.createUserIfNotExists();

        // 検証
        assertEquals(existingUser, result);
        verify(userRepository, times(1)).findByCognitoSub(cognitoSub);
        verify(userRepository, times(0)).save(any(User.class));
    }
}

