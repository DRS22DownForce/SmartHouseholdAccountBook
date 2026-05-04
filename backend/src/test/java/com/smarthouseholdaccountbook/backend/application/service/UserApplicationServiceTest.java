package com.smarthouseholdaccountbook.backend.application.service;

import com.smarthouseholdaccountbook.backend.auth.provider.CurrentAuthProvider;
import com.smarthouseholdaccountbook.backend.entity.User;
import com.smarthouseholdaccountbook.backend.exception.UserNotFoundException;
import com.smarthouseholdaccountbook.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
    private CacheManager cacheManager;

    @Mock
    private Cache userEnsuredCache;

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
        // 「確認済みキャッシュ」のふるまいを ConcurrentHashMap で簡易再現する。
        // これにより「1回目の呼び出しでキャッシュに載り、2回目以降は DB を叩かない」挙動をテストできる。
        ConcurrentMap<Object, Object> backing = new ConcurrentHashMap<>();
        // テストケースの中にこのstubを使わないものがあるのでstubが未使用でも許容されるlenient() を使う。
        lenient().when(cacheManager.getCache("userEnsured")).thenReturn(userEnsuredCache);
        lenient().when(userEnsuredCache.get(any())).thenAnswer(inv -> {
            Object value = backing.get(inv.getArgument(0));
            if (value == null) {
                return null;
            }
            // Cache#get(Object) は ValueWrapper を返す契約。ラムダに明示的な戻り型を与える
            Cache.ValueWrapper wrapper = () -> value;
            return wrapper;
        });
        lenient().doAnswer(inv -> {
            backing.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(userEnsuredCache).put(any(), any());
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

        // save() に渡された User の中身を検証する
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(cognitoSub, saved.getCognitoSub());
        assertEquals(email, saved.getEmail());
    }

    @Test
    void ensureUserExists_ユーザーが存在する場合は何もしない() {
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        userApplicationService.ensureUserExists();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void ensureUserExists_2回目以降はキャッシュが効いてDBアクセスしない() {
        User existingUser = new User(cognitoSub, email);
        when(userRepository.findByCognitoSub(cognitoSub)).thenReturn(Optional.of(existingUser));

        // 1回目: DB を引いて「存在」を確認し、キャッシュに載せる
        userApplicationService.ensureUserExists();
        // 2回目: キャッシュヒットで DB アクセスなし
        userApplicationService.ensureUserExists();
        // 3回目: 同上
        userApplicationService.ensureUserExists();

        verify(userRepository, times(1)).findByCognitoSub(cognitoSub);
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
