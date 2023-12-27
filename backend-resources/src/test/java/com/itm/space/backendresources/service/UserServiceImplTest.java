package com.itm.space.backendresources.service;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
//ssd

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private Keycloak keycloak;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RealmResource realmResource;

    @Value("${keycloak.realm}")
    private String realm;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private MappingsRepresentation mappingsRepresentation;

    @Mock
    private UserMapper userMapper;


    private UserRequest userRequestTest = UserRequest.builder()
            .username("test")
            .firstName("test")
            .lastName("test")
            .email("test@gmail.com")
            .password("test")
            .build();
    ;

    private UserResponse userResponseTest = UserResponse.builder()
            .firstName("test")
            .lastName("test")
            .email("test@gmail.com")
            .roles(List.of("test"))
            .groups(List.of("test"))
            .build();
    ;


    private String testId = "515c3ab4-f966-11ed-be56-0242ac120002";


    @Test
    public void shouldCreateUser() throws Exception {

        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.created(new URI(testId)).build());

        userService.createUser(userRequestTest);

        verify(keycloak).realm(realm);
        verify(realmResource, times(1)).users();
        verify(usersResource, times(1)).create(any());

    }


    @Test
    public void shouldGetUserById() {
        UserRepresentation userRepresentation = new UserRepresentation();
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        GroupRepresentation groupRepresentation = new GroupRepresentation();

        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(testId)).thenReturn(userResource);


        userRepresentation.setId(testId);
        userRepresentation.setEmail("test@gmail.com");
        userRepresentation.setFirstName("test");
        userRepresentation.setLastName("test");

        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        roleRepresentation.setName("test");

        when(mappingsRepresentation.getRealmMappings()).thenReturn(List.of(roleRepresentation));

        groupRepresentation.setName("test");

        when(userResource.groups()).thenReturn(List.of(groupRepresentation));


        when(userMapper.userRepresentationToUserResponse
                (userRepresentation, List.of(roleRepresentation), List.of(groupRepresentation)))
                .thenReturn(userResponseTest);

        UserResponse userResponse = userService.getUserById(UUID.fromString(testId));

        assertAll(
                () -> assertThat(userResponse.getFirstName()).isEqualTo(userResponseTest.getFirstName()),
                () -> assertThat(userResponse.getLastName()).isEqualTo(userResponseTest.getLastName()),
                () -> assertThat(userResponse.getEmail()).isEqualTo(userResponseTest.getEmail()),
                () -> assertThat(userResponse.getRoles()).isEqualTo(userResponse.getRoles()),
                () -> assertThat(userResponse.getGroups()).isEqualTo(userResponseTest.getGroups())
        );

    }

    @Test
    public void shouldPreparePasswordRepresentation()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String password = "test";

        Method method = UserServiceImpl.class.getDeclaredMethod("preparePasswordRepresentation", String.class);
        method.setAccessible(true);

        CredentialRepresentation credentialRepresentation = (CredentialRepresentation)
                method.invoke(userService, password);

        assertFalse(credentialRepresentation.isTemporary());
        assertEquals(CredentialRepresentation.PASSWORD, credentialRepresentation.getType());
        assertEquals(password, credentialRepresentation.getValue());

    }

    @Test
    public void shouldPrepareUserRepresentation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue("test");

        Method method = UserServiceImpl.class.getDeclaredMethod("prepareUserRepresentation",
                UserRequest.class, CredentialRepresentation.class);
        method.setAccessible(true);


        UserRepresentation userRepresentation = (UserRepresentation) method
                .invoke(userService, userRequestTest, credentialRepresentation);

        assertAll(
                () -> assertNotNull(userRepresentation),
                () -> assertEquals("test", userRepresentation.getUsername()),
                () -> assertEquals("test@gmail.com", userRepresentation.getEmail()),
                () -> assertEquals(List.of(credentialRepresentation), userRepresentation.getCredentials()),
                () -> assertEquals("test", userRepresentation.getFirstName()),
                () -> assertEquals("test", userRepresentation.getLastName())
        );

    }

}
