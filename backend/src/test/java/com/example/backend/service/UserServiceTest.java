package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.auth.provider.CurrentAuthProvider;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentAuthProvider currentAuthProvider;

    @InjectMocks
    //テスト対象のオブジェクトにモックを注入する
    private UserService userService;

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

        //findByCognitoSubが空を返すようにモックを設定
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.empty());
        
        //saveが呼び出されたとき
        User savedUser = new User(cognitoSub, email);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        //createUserIfNotExistsを呼び出す
        User result = userService.createUserIfNotExists();

        assertEquals(savedUser, result);
    }

    @Test
    void createUserIfNotExists_ユーザーが存在する場合はデータベースに保存しない() {
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        User result = userService.createUserIfNotExists();

        assertEquals(existingUser, result);
        verify(userRepository, times(1)).findByCognitoSub(cognitoSub);
        verify(userRepository, times(0)).save(any(User.class));
    }
    
}
