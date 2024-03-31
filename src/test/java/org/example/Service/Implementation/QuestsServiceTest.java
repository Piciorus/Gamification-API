package org.example.Service.Implementation;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Repository.QuestsRepository;
import org.example.Repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class QuestsServiceTest {

    @Mock
    private QuestsRepository questsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private QuestsService questsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests the createQuest method with a positive integer rewardTokens.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_ECP_PositiveInteger() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(10); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(5); // Valid reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }

    /**
     * Tests the createQuest method with the maximum possible integer value for rewardTokens.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_ECP_MaximumValue() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(Integer.MAX_VALUE); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(Integer.MAX_VALUE); // Maximum value for reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }

    /**
     * Tests the createQuest method with the minimum positive integer value for rewardTokens.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_ECP_MinimumPositiveInteger() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(1); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(1); // Minimum positive integer value for reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }

    /**
     * Tests the createQuest method with zero rewardTokens.
     * The method should throw a RuntimeException.
     */
    @Test
    public void testCreateQuest_ECP_Zero() {
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(0); // Zero reward tokens

        assertThrows(RuntimeException.class, () -> questsService.createQuest(request, UUID.randomUUID()));
    }

    /**
     * Tests the createQuest method with a negative integer for rewardTokens.
     * The method should throw a RuntimeException.
     */
    @Test
    public void testCreateQuest_ECP_NegativeInteger() {
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(-5); // Negative reward tokens

        assertThrows(RuntimeException.class, () -> questsService.createQuest(request, UUID.randomUUID()));
    }

    /**
     * Tests the createQuest method with an existing user ID.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_ECP_ExistingUserId() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(10); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(5); // Valid reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }

    /**
     * Tests the createQuest method with a non-existent user ID.
     * The method should throw a RuntimeException.
     */
    @Test
    public void testCreateQuest_ECP_NonExistingUserId() {
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(null); // User not found

        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(5); // Valid reward tokens

        assertThrows(RuntimeException.class, () -> questsService.createQuest(request, userId));
    }

    /**
     * Tests the createQuest method with the lower boundary value (1) for rewardTokens.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_BVA_LowerBoundary() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(10); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(1); // Lower boundary value for reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }

    /**
     * Tests the createQuest method with the upper boundary value (Integer.MAX_VALUE) for rewardTokens.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_BVA_UpperBoundary() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(Integer.MAX_VALUE); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(Integer.MAX_VALUE); // Upper boundary value for reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }

    /**
     * Tests the createQuest method with the value just below the lower boundary (0) for rewardTokens.
     * The method should throw a RuntimeException.
     */
    @Test
    public void testCreateQuest_BVA_JustBelowLowerBoundary() {
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(0); // Just below lower boundary for reward tokens

        assertThrows(RuntimeException.class, () -> questsService.createQuest(request, UUID.randomUUID()));
    }

    /**
     * Tests the createQuest method with the value just above the upper boundary (Integer.MAX_VALUE + 1) for rewardTokens.
     * The method should successfully create a quest.
     */
    @Test
    public void testCreateQuest_BVA_JustAboveUpperBoundary() {
        // Mock user with enough tokens
        User user = new User();
        user.setTokens(10); // Assuming user has enough tokens
        UUID userId = UUID.randomUUID();
        when(usersRepository.getById(userId)).thenReturn(user);

        // Mock successful quest creation
        CreateQuestRequest request = new CreateQuestRequest();
        request.setRewardTokens(Integer.MAX_VALUE + 1); // Just above upper boundary for reward tokens
        Quest mockQuest = new Quest(); // Mock quest object
        when(mapper.CreateQuestRequestToQuest(request)).thenReturn(mockQuest); // Mock mapper behavior
        when(questsRepository.save(mockQuest)).thenReturn(mockQuest); // Mock saving the quest

        // Call the method and assert
        assertNotNull(questsService.createQuest(request, userId));

        // Verify interactions
        verify(usersRepository, times(1)).getById(userId);
        verify(mapper, times(1)).CreateQuestRequestToQuest(request);
        verify(questsRepository, times(1)).save(mockQuest);
    }
}


