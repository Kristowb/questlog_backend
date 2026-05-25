package com.questlog.backend.service.impl;

import com.questlog.backend.dto.QuestResponse;
import com.questlog.backend.exception.BadRequestException;
import com.questlog.backend.exception.ResourceNotFoundException;
import com.questlog.backend.model.Quest;
import com.questlog.backend.model.User;
import com.questlog.backend.repository.QuestRepository;
import com.questlog.backend.repository.UserRepository;
import com.questlog.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceImplTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuestServiceImpl questService;

    private User testUser;
    private Quest testQuest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("hero@quest.com")
                .name("Arthur")
                .classType("WARRIOR")
                .level(1)
                .build();

        testQuest = Quest.builder()
                .id(10L)
                .userId(1L)
                .title("Heavy Lifting Day")
                .type("STRENGTH")
                .xpReward(45)
                .questDate(LocalDate.now())
                .isCompleted(false)
                .build();
    }

    @Test
    void getDailyQuests_ExistingQuests_ReturnsThem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questRepository.findByUserIdAndQuestDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(testQuest));

        List<QuestResponse> dailyQuests = questService.getDailyQuests(1L);

        assertThat(dailyQuests).hasSize(1);
        assertThat(dailyQuests.get(0).title()).isEqualTo("Heavy Lifting Day");
        verify(questRepository, never()).saveAll(anyList());
    }

    @Test
    void getDailyQuests_EmptyQuests_GeneratesWarriorQuests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questRepository.findByUserIdAndQuestDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(questRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<QuestResponse> dailyQuests = questService.getDailyQuests(1L);

        assertThat(dailyQuests).hasSize(4);
        assertThat(dailyQuests.get(0).title()).isEqualTo("Heavy Lifting Day");
        verify(questRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getDailyQuests_EmptyQuests_GeneratesArcherQuests() {
        testUser.setClassType("ARCHER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questRepository.findByUserIdAndQuestDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(questRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<QuestResponse> dailyQuests = questService.getDailyQuests(1L);

        assertThat(dailyQuests).hasSize(4);
        assertThat(dailyQuests.get(0).title()).isEqualTo("Wind Runner");
        verify(questRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getDailyQuests_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.getDailyQuests(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User dengan ID 99 tidak ditemukan");
    }

    @Test
    void completeQuest_Success() {
        when(questRepository.findById(10L)).thenReturn(Optional.of(testQuest));
        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestResponse response = questService.completeQuest(10L);

        assertThat(response.isCompleted()).isTrue();
        verify(userService, times(1)).addXp(1L, 45, "STRENGTH");
        verify(questRepository, times(1)).save(testQuest);
    }

    @Test
    void completeQuest_AlreadyCompleted_ThrowsException() {
        testQuest.setCompleted(true);
        when(questRepository.findById(10L)).thenReturn(Optional.of(testQuest));

        assertThatThrownBy(() -> questService.completeQuest(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Quest ini sudah diselesaikan sebelumnya");
        verify(userService, never()).addXp(anyLong(), anyInt(), anyString());
    }

    @Test
    void completeQuest_NotFound_ThrowsException() {
        when(questRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.completeQuest(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quest dengan ID 99 tidak ditemukan");
    }
}
