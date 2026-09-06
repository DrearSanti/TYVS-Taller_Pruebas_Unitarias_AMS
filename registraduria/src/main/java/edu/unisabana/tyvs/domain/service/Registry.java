package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

/**
 * Servicio de dominio que decide si una persona puede quedar registrada
 * como votante.
 *
 * Las reglas se evaluan en orden (R1 -> R7) y la PRIMERA que falla determina
 * el resultado. Por eso una persona muerta de 15 anios devuelve DEAD y no
 * UNDERAGE: es una decision de diseno, documentada en el Wiki.
 */
public class Registry {

    /** Edad minima para ejercer el voto. */
    private static final int MIN_AGE = 18;

    /** Rango biologicamente posible para una edad. */
    private static final int MIN_VALID_AGE = 0;
    private static final int MAX_VALID_AGE = 120;
    
    /** Identificador minimo valido. */
    private static final int MIN_VALID_ID = 1;

    public RegisterResult registerVoter(Person p) {
        if (p == null) {
            return RegisterResult.INVALID; // regla defensiva
        }
        if (p.getId() < MIN_VALID_ID) {
            return RegisterResult.INVALID;
        }
        if (!p.isAlive()) {
            return RegisterResult.DEAD;
        }
        if (p.getAge() < MIN_VALID_AGE || p.getAge() > MAX_VALID_AGE) {
            return RegisterResult.INVALID_AGE;
        }
        if (p.getAge() < MIN_AGE) {
            return RegisterResult.UNDERAGE;
        }
        // TODO iteracion 6: detectar identificadores duplicados.
        return RegisterResult.VALID;
    }
}