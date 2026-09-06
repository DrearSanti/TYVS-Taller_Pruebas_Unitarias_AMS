package edu.unisabana.tyvs.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.unisabana.tyvs.domain.model.Gender;
import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

/**
 * Pruebas por EJEMPLO del dominio: cada prueba fija una entrada concreta y su
 * resultado esperado. Incluye las validaciones de identificador y duplicados.
 *
 * Complemento: RegistryPropertiesTest expresa las mismas reglas como
 * PROPIEDADES sobre rangos completos de entradas, en vez de ejemplos sueltos.
 */
class RegistryTest {

    private Registry registry;

    /**
     * Crea un Registry nuevo antes de cada prueba.
     *
     * Registry guarda los identificadores aceptados en un Set de instancia.
     * Crear una instancia nueva evita que los registros de una prueba
     * afecten a las siguientes y mantiene su independencia.
     *
     * El Set no debe ser static: compartiria los identificadores entre
     * instancias incluso al crear un Registry nuevo en cada prueba.
     */
    @BeforeEach
    void setUp() {
        registry = new Registry();
    }

        @Test
    @DisplayName("Una persona viva y mayor de edad queda registrada")
    void shouldRegisterValidPerson() {
        // Arrange: preparar los datos
        Person person = new Person("Ana", 1, 30, Gender.FEMALE, true);

        // Act: ejecutar la accion que queremos probar
        RegisterResult result = registry.registerVoter(person);

        // Assert: verificar el resultado esperado
        assertEquals(RegisterResult.VALID, result);
    }

       @Test
    @DisplayName("Una persona no viva se rechaza con DEAD")
    void shouldRejectDeadPerson() {
        // Arrange: preparar los datos
        Person dead = new Person("Carlos", 2, 40, Gender.MALE, false);

        // Act: ejecutar la accion que queremos probar
        RegisterResult result = registry.registerVoter(dead);

        // Assert: verificar el resultado esperado
        assertEquals(RegisterResult.DEAD, result);
    }

       @Test
    @DisplayName("Una persona nula se rechaza con INVALID")
    void shouldReturnInvalidWhenPersonIsNull() {
        // Act
        RegisterResult result = registry.registerVoter(null);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

        @Test
    @DisplayName("Una persona de 17 años se rechaza con UNDERAGE")
    void shouldRejectUnderageAt17() {
        // Arrange: persona viva, id valido, un anio por debajo del limite
        Person menor = new Person("Laura", 7, 17, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(menor);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
    }

        @Test
    @DisplayName("Una edad negativa se rechaza con INVALID_AGE")
    void shouldRejectInvalidAgeBelowZero() {
        // Arrange: persona viva, id valido, edad biologicamente imposible
        Person imposible = new Person("Diego", 4, -1, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(imposible);

        // Assert
        assertEquals(RegisterResult.INVALID_AGE, result);
    }

        @Test
    @DisplayName("Una edad mayor a 120 se rechaza con INVALID_AGE")
    void shouldRejectInvalidAgeOver120() {
        // Arrange: borde superior del rango biologico, un anio por encima
        Person imposible = new Person("Elena", 5, 121, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(imposible);

        // Assert
        assertEquals(RegisterResult.INVALID_AGE, result);
    }

        @Test
    @DisplayName("Un recien nacido se rechaza con UNDERAGE")
    void shouldRejectUnderageAtZero() {
        // Arrange: borde inferior de la clase "menor de edad"
        Person bebe = new Person("Mateo", 6, 0, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(bebe);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
    }

        @Test
    @DisplayName("Una persona de 18 anios queda registrada")
    void shouldAcceptAdultAt18() {
        // Arrange: borde inferior de la clase "mayor de edad"
        Person adulto = new Person("Sofia", 8, 18, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(adulto);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }

        @Test
    @DisplayName("Una persona de 120 anios queda registrada")
    void shouldAcceptMaxAge120() {
        // Arrange: borde superior valido del rango biologico
        Person longevo = new Person("Julio", 9, 120, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(longevo);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }
        @Test
    @DisplayName("Un documento con numero cero se rechaza con INVALID")
    void shouldRejectWhenIdIsZero() {
        // Arrange
        Person sinDocumento = new Person("Pedro", 0, 25, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(sinDocumento);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

        @Test
    @DisplayName("Un documento con numero negativo se rechaza con INVALID")
    void shouldRejectWhenIdIsNegative() {
        // Arrange
        Person documentoInvalido = new Person("Rosa", -5, 25, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(documentoInvalido);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

        @Test
    @DisplayName("Un documento ya registrado se rechaza con DUPLICATED")
    void shouldRejectDuplicatedId() {
        // Arrange: ya hay una persona registrada con este documento
        Person primera = new Person("Luis", 777, 25, Gender.MALE, true);
        Person segunda = new Person("Luis", 777, 25, Gender.MALE, true);
        registry.registerVoter(primera);

        // Act
        RegisterResult result = registry.registerVoter(segunda);

        // Assert
        assertEquals(RegisterResult.DUPLICATED, result);
    }

        @Test
    @DisplayName("Un documento distinto se acepta despues de otro registro")
    void shouldAcceptDifferentIdAfterRegistration() {
        // Arrange: ya hay una persona registrada con otro documento
        registry.registerVoter(new Person("Luis", 777, 25, Gender.MALE, true));
        Person otra = new Person("Marta", 778, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(otra);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }

}