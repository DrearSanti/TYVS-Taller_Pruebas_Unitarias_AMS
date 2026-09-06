package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Gender;
import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PRUEBAS BASADAS EN PROPIEDADES (jqwik).
 *
 * Una prueba por ejemplo dice "edad 40 y no viva -> DEAD".
 * Una propiedad dice "para TODA edad y TODO genero, si no esta viva -> DEAD",
 * y jqwik genera cientos de combinaciones para intentar refutarla.
 *
 * Es la continuacion natural de las clases de equivalencia: usted eligio un
 * representante por clase a mano; aqui la maquina explora la clase entera.
 *
 * Cuando una propiedad falla, jqwik no reporta la entrada aleatoria que la
 * rompio, sino la MAS SIMPLE que la rompe (shrinking). Si "edad 73 con nombre
 * 'xkqz'" falla, le reportara "edad 0 con nombre ''", que es mucho mas facil
 * de diagnosticar.
 *
 * Las tres primeras propiedades vienen del repositorio base. Las cinco
 * siguientes son las del equipo (entregable 4b).
 */
class RegistryPropertiesTest {

    /** Genera cualquier valor del enum Gender, incluido UNIDENTIFIED. */
    @Provide
    Arbitrary<Gender> generos() {
        return Arbitraries.of(Gender.values());
    }

    /** Nombres arbitrarios, incluida la cadena vacia. */
    @Provide
    Arbitrary<String> nombres() {
        return Arbitraries.strings().alpha().ofMaxLength(20);
    }

    // ------------------------------------------------------------------
    // Propiedades de referencia (repositorio base)
    // ------------------------------------------------------------------

    /**
     * Regla R3: una persona no viva se rechaza SIEMPRE, sin importar su edad,
     * su documento ni su genero.
     */
    @Property
    void unaPersonaNoVivaSiempreEsRechazada(
            @ForAll("nombres") String nombre,
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 0, max = 120) int edad,
            @ForAll("generos") Gender genero) {

        Person muerta = new Person(nombre, id, edad, genero, false);

        assertEquals(RegisterResult.DEAD, new Registry().registerVoter(muerta));
    }

    /**
     * Propiedad de DETERMINISMO: registrar la misma persona en dos Registry
     * recien creados produce el mismo resultado.
     */
    @Property
    void elResultadoNoDependeDeLaInstancia(
            @ForAll("nombres") String nombre,
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 0, max = 120) int edad,
            @ForAll("generos") Gender genero,
            @ForAll boolean viva) {

        Person p = new Person(nombre, id, edad, genero, viva);

        RegisterResult primera = new Registry().registerVoter(p);
        RegisterResult segunda = new Registry().registerVoter(p);

        assertEquals(primera, segunda);
    }

    /**
     * Propiedad de TOTALIDAD: registerVoter nunca devuelve null ni lanza una
     * excepcion, sea cual sea la entrada.
     */
    @Property
    void nuncaDevuelveNullNiLanzaExcepcion(
            @ForAll("nombres") String nombre,
            @ForAll int id,
            @ForAll int edad,
            @ForAll("generos") Gender genero,
            @ForAll boolean viva) {

        Person p = new Person(nombre, id, edad, genero, viva);

        RegisterResult resultado = new Registry().registerVoter(p);

        org.junit.jupiter.api.Assertions.assertNotNull(resultado);
    }

    // ------------------------------------------------------------------
    // Propiedades del equipo (entregable 4b)
    // ------------------------------------------------------------------

    /**
     * R5. Todo menor de edad con datos por lo demas validos se rechaza como
     * UNDERAGE.
     *
     * El rango de edad 0..17 es exactamente la clase de equivalencia
     * "menor de edad" de la tabla del Wiki. La prueba por ejemplo
     * shouldRejectUnderageAt17 verifica un representante; esta verifica los
     * dieciocho valores de la clase, con cualquier documento y cualquier
     * genero.
     */
    @Property
    void todoMenorDeEdadEsRechazado(
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 0, max = 17) int edad,
            @ForAll("generos") Gender genero) {

        Person menor = new Person("X", id, edad, genero, true);

        assertEquals(RegisterResult.UNDERAGE, new Registry().registerVoter(menor));
    }

    /**
     * R5 y R7. Todo adulto vivo, con documento positivo y edad dentro del
     * rango biologico, queda registrado.
     *
     * Es la contraparte positiva de la anterior: juntas afirman que la
     * frontera de los 18 anios parte el rango 0..120 sin huecos ni solapes.
     */
    @Property
    void todoAdultoValidoSeRegistra(
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 18, max = 120) int edad,
            @ForAll("generos") Gender genero) {

        Person adulto = new Person("X", id, edad, genero, true);

        assertEquals(RegisterResult.VALID, new Registry().registerVoter(adulto));
    }

    /**
     * R4. Toda edad por encima del maximo biologico se rechaza como
     * INVALID_AGE, nunca como VALID ni como UNDERAGE.
     *
     * El rango llega hasta 1000 y no hasta Integer.MAX_VALUE a proposito: el
     * interes esta en la clase de equivalencia "edad imposible", no en probar
     * el desbordamiento de int, que ya cubre nuncaDevuelveNullNiLanzaExcepcion.
     */
    @Property
    void todaEdadImposibleEsRechazada(
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 121, max = 1000) int edad,
            @ForAll("generos") Gender genero) {

        Person imposible = new Person("X", id, edad, genero, true);

        assertEquals(RegisterResult.INVALID_AGE, new Registry().registerVoter(imposible));
    }

    /**
     * PROPIEDAD ESTRUCTURAL. Un rechazo no deja huella en el Registry: repetir
     * la misma llamada rechazada sobre la MISMA instancia devuelve siempre el
     * mismo resultado.
     *
     * Es la unica propiedad que reutiliza la instancia en vez de crear una
     * nueva, y por eso es la unica que puede detectar un efecto de borde en el
     * estado interno. Protege la Decision 3 del equipo: solo se almacenan los
     * documentos de personas efectivamente registradas. Si al implementar R6
     * el id se guardara antes de evaluar las demas reglas, un menor rechazado
     * quedaria ocupando su propio documento y esta propiedad lo delataria.
     *
     * Se restringe a edades 0..17 (que siempre son rechazo) porque un VALID
     * NO es idempotente una vez exista R6: el segundo intento debe devolver
     * DUPLICATED. Esa asimetria es intencional y esta documentada en el Wiki.
     */
    @Property
    void unRechazoNoDejaHuellaEnElRegistro(
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 0, max = 17) int edad,
            @ForAll("generos") Gender genero) {

        Registry registry = new Registry();
        Person menor = new Person("X", id, edad, genero, true);

        RegisterResult primera = registry.registerVoter(menor);
        RegisterResult segunda = registry.registerVoter(menor);

        assertEquals(primera, segunda);
    }

    /**
     * R2. Todo documento no positivo se rechaza como INVALID.
     *
     * ESTA PROPIEDAD ESTA EN ROJO A PROPOSITO. La regla R2 aun no esta
     * implementada, de modo que hoy devuelve VALID y jqwik reporta el
     * contraejemplo reducido. Es TDD aplicado a propiedades: la propiedad se
     * escribe antes que la implementacion y pasa a verde sola cuando la
     * guarda de identificador entre al codigo.
     */
    @Property
    void todoIdNoPositivoEsRechazado(
            @ForAll @IntRange(min = -1000, max = 0) int id,
            @ForAll @IntRange(min = 18, max = 120) int edad,
            @ForAll("generos") Gender genero) {

        Person sinDocumento = new Person("X", id, edad, genero, true);

        assertEquals(RegisterResult.INVALID, new Registry().registerVoter(sinDocumento));
    }
}