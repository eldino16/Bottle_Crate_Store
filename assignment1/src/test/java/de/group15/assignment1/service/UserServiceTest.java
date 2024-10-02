package de.group15.assignment1.service;

import de.group15.assignment1.model.Address;
import de.group15.assignment1.model.User;
import de.group15.assignment1.model.UserDTO;
import de.group15.assignment1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User max;
    private User updatedUser;
    private UserDTO dto;

    @BeforeEach
    public void initCommonUsedData() {
        Address address = new Address(null, "An der Spinnerei", "13", "96049");
        Address address2 = new Address(null, "An der Test", "15", "96000");
        max = new User();
        max.setUsername("Max");
        max.setPassword("123456");
        max.setRole("CUSTOMER");
        max.setBirthday(LocalDate.of(1996, 8, 2));
        max.setBillingaddresses(Collections.singleton(address2));
        max.setDeliveryaddresses(Collections.singleton(address));

        updatedUser = new User();
        updatedUser.setUsername("Max");
        updatedUser.setPassword("123456");
        updatedUser.setRole("CUSTOMER");
        updatedUser.setBirthday(LocalDate.of(1996, 8, 2));
        updatedUser.setBillingaddresses(Collections.singleton(address));
        updatedUser.setDeliveryaddresses(Collections.singleton(address2));

        dto = new UserDTO("password", LocalDate.now(), "mail@mail.de", "Username", Collections.singletonList(address2), Collections.singletonList(address));

    }

    @Test
    public void registerUser_ShouldSuccess() {
        when(this.userRepository.existsByUsername(max.getUsername())).thenReturn(false);
        when(this.userRepository.save(max)).thenReturn(max);

        User newUser = userService.registerUser(max);

        verify(this.userRepository, times(1)).existsByUsername(max.getUsername());
        verify(this.userRepository, times(1)).save(max);
        assertEquals(newUser, max);
    }


    @Test
    public void registerUser_ShouldFailWhenUserAlreadyExists() {
        when(this.userRepository.existsByUsername(max.getUsername())).thenReturn(true);

        User newUser = userService.registerUser(max);

        verify(this.userRepository, times(1)).existsByUsername(max.getUsername());
        assertEquals(newUser, new User());
    }

    @Test
    public void updateUser_ShouldSuccess() {
        when(this.userRepository.findById(max.getUsername())).thenReturn(Optional.ofNullable(max));
        when(this.userRepository.save(updatedUser)).thenReturn(updatedUser);

        User user = userService.updateUser(max.getUsername(), dto);

        verify(this.userRepository, times(2)).findById(max.getUsername());
        verify(this.userRepository, times(1)).save(max);
        assertEquals(user.getBillingaddresses(), updatedUser.getBillingaddresses());
    }

    @Test
    public void updateUser_ShouldFailWhenUserDoesntExist() {
        when(this.userRepository.findById("May")).thenReturn(Optional.empty());

        User user = userService.updateUser("May", dto);

        verify(this.userRepository, times(1)).findById("May");
        assertNull(user);
    }

    @Test
    public void getUser_ShouldSuccess() {
        when(this.userRepository.findById(max.getUsername())).thenReturn(Optional.ofNullable(max));
        when(this.userRepository.getUserWithEntitiesByUsername(max.getUsername())).thenReturn(Optional.ofNullable(max));

        User user = userService.getUser(max.getUsername());

        verify(this.userRepository, times(1)).findById(max.getUsername());
        verify(this.userRepository, times(1)).getUserWithEntitiesByUsername(max.getUsername());
        assertEquals(user, max);
    }

    @Test
    public void getUser_ShouldFailWhenUserDoesntExist() {
        when(this.userRepository.findById("May")).thenReturn(Optional.empty());

        User user = userService.getUser("May");

        verify(this.userRepository, times(1)).findById("May");
        assertNull(user);
    }


    @Test
    public void loadUserByUsername_ShouldSuccess() {
        when(this.userRepository.getUserWithEntitiesByUsername(max.getUsername())).thenReturn(Optional.ofNullable(max));

        UserDetails details = userService.loadUserByUsername(max.getUsername());

        verify(this.userRepository, times(1)).getUserWithEntitiesByUsername(max.getUsername());
        assertEquals(details, max);
    }


    @Test
    public void loadUserByUsername_ShouldThrowUsernameNotFoundExceptionIfUserDoesntExists() {
        when(this.userRepository.getUserWithEntitiesByUsername("May")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("May");
        });

        verify(this.userRepository, times(1)).getUserWithEntitiesByUsername("May");
        assertEquals(exception.getMessage(), "User 'May' not found!");
    }

    @Test
    @WithMockUser(username = "Max", roles = "CUSTOMER")
    public void getCurrentUser_ShouldSuccess() throws AuthenticationException {
        Authentication auth = userService.getCurrentUser();
        assertEquals(auth.getName(), "Max");
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    public void getCurrentUser_ShouldThrowAuthenticationExceptionOnAnonymousUser() {
        Exception exception = assertThrows(AuthenticationException.class, () -> {
            userService.getCurrentUser();
        });
        assertEquals(exception.getMessage(), "No user logged in");
    }
}
