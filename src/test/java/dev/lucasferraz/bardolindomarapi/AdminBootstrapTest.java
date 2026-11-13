package dev.lucasferraz.bardolindomarapi;

import dev.lucasferraz.bardolindomarapi.config.AdminBootstrap;
import dev.lucasferraz.bardolindomarapi.repositories.*;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdminBootstrapTest {
    @Test
    void exigeConfiguracaoQuandoNaoExisteAdmin() {
        var bootstrap = new AdminBootstrap(mock(UsuarioRepository.class), mock(RoleRepository.class),
                mock(PasswordEncoder.class), mock(Validator.class), "", "", "");
        assertThatThrownBy(() -> bootstrap.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("ADMIN_NOME");
    }

    @Test
    void naoPromoveUsuarioExistente() {
        var usuarios = mock(UsuarioRepository.class);
        when(usuarios.findByEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(mock(dev.lucasferraz.bardolindomarapi.models.Usuario.class)));
        var bootstrap = new AdminBootstrap(usuarios, mock(RoleRepository.class), mock(PasswordEncoder.class),
                mock(Validator.class), "Admin", "cliente@teste.com", "senha-valida-123");
        assertThatThrownBy(() -> bootstrap.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sem permissão administrativa");
        verify(usuarios, never()).saveAndFlush(any());
    }
}
