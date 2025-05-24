package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    //テスト対象のオブジェクトにモックを注入する
    private UserService userService;
    
    @Test
    void createUserIfNotExists_ユーザーが存在しない場合はデータベースに保存() {
        String cognitoSub = "cognitoSub";
        String email = "test@example.com";
        //findByCognitoSubが空を返すようにモックを設定
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.empty());
        
        //saveが呼び出されたとき
        User savedUser = new User(cognitoSub, email);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        //createUserIfNotExistsを呼び出す
        User result = userService.createUserIfNotExists(cognitoSub, email);

        assertEquals(savedUser, result);
    }

    @Test
    void createUserIfNotExists_ユーザーが存在する場合はデータベースに保存しない() {
        String cognitoSub = "cognitoSub";
        String email = "test@example.com";
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        User result = userService.createUserIfNotExists(cognitoSub, email);

        assertEquals(existingUser, result);
        verify(userRepository, times(1)).findByCognitoSub(cognitoSub);
        verify(userRepository, times(0)).save(any(User.class));
    }
    
}
