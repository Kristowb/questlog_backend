package com.questlog.backend.service.impl;

import com.questlog.backend.dto.UserResponse;
import com.questlog.backend.exception.BadRequestException;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.User;
import com.questlog.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("hero@quest.com")
                .name("Arthur")
                .classType(null)
                .level(1)
                .strengthXp(0)
                .vitalityXp(0)
                .xpToNextLevel(100)
                .coins(0)
                .isPremium(false)
                .build();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("hero@quest.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User dengan ID 99 tidak ditemukan");
    }

    @Test
    void chooseClass_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.chooseClass(1L, "WARRIOR");

        assertThat(response.classType()).isEqualTo("WARRIOR");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void chooseClass_AlreadyChosen_ThrowsException() {
        testUser.setClassType("WARRIOR");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.chooseClass(1L, "ARCHER"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Kelas pahlawan sudah pernah dipilih");
    }

    @Test
    void chooseClass_InvalidClass_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.chooseClass(1L, "MAGE"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Jenis kelas tidak valid");
    }

    @Test
    void addXp_Strength_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.addXp(1L, 40, "STRENGTH");

        assertThat(response.strengthXp()).isEqualTo(40);
        assertThat(response.level()).isEqualTo(1);
    }

    @Test
    void addXp_LevelUp_Success() {
        testUser.setClassType("WARRIOR");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.addXp(1L, 120, "STRENGTH");

        assertThat(response.level()).isEqualTo(2);
        assertThat(response.coins()).isEqualTo(20);
        assertThat(response.strengthXp()).isEqualTo(10);
        assertThat(response.vitalityXp()).isEqualTo(10);
        assertThat(response.xpToNextLevel()).isEqualTo(200);
    }

    @Test
    void getLeaderboard_Success() {
        User u2 = User.builder().level(5).strengthXp(10).build();
        when(userRepository.findAllByOrderByLevelDescStrengthXpDesc()).thenReturn(List.of(u2, testUser));

        List<UserResponse> leaderboard = userService.getLeaderboard();

        assertThat(leaderboard).hasSize(2);
        assertThat(leaderboard.get(0).level()).isEqualTo(5);
    }
}
