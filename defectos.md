# Registro de defectos

Defectos detectados durante el desarrollo mediante TDD del proyecto Registraduría. Los resultados obtenidos corresponden al momento de detección, antes de aplicar la corrección.

## Defecto 01 — Orden de las validaciones de edad

- **Caso:** persona viva con edad -1.
- **Resultado esperado:** `INVALID_AGE`.
- **Resultado obtenido:** `UNDERAGE`.
- **Causa:** la validación de mayoría de edad (R5) se evaluaba antes que la validación del rango de edad (R4). Una edad negativa cumplía la condición de ser menor de 18.
- **Detección:** prueba `shouldRejectInvalidAgeBelowZero`, iteración 4 de Santiago, según el documento de asignación del equipo.
- **Corrección:** mover la validación R4 antes de R5.
- **Estado:** Resuelto. La prueba pasa en la ejecución actual.

## Defecto 02 — Aceptación de identificadores no positivos

- **Caso:** personas vivas de 25 años con identificadores 0 y -5.
- **Resultado esperado:** `INVALID` en ambos casos.
- **Resultado obtenido:** `VALID` en ambos casos.
- **Causa:** faltaba validar que el identificador fuera positivo.
- **Detección:** pruebas `shouldRejectWhenIdIsZero` y `shouldRejectWhenIdIsNegative`, iteración 5.
- **Evidencia RED:** 14 pruebas ejecutadas, 2 fallos y 0 errores; `expected: <INVALID> but was: <VALID>`.
- **Corrección:** validar el identificador después de comprobar la nulidad y antes de comprobar si la persona está viva. En el refactor se extrajo `MIN_VALID_ID = 1`.
- **Estado:** Resuelto. Ambas pruebas pasan.
- **Commits:** RED `bbd79ee`, GREEN `3281578`, REFACTOR `5936e29`.

## Defecto 03 — Aceptación de un documento ya registrado

- **Caso:** registrar dos veces, en la misma instancia de Registry, personas vivas de 25 años con identificador 777.
- **Resultado esperado:** `VALID` en el primer registro y `DUPLICATED` en el segundo.
- **Resultado obtenido:** el segundo registro devolvía `VALID`.
- **Causa:** Registry no almacenaba los identificadores aceptados ni comprobaba su existencia.
- **Detección:** prueba `shouldRejectDuplicatedId`, iteración 6.
- **Evidencia RED:** 16 pruebas ejecutadas, 1 fallo y 0 errores; `expected: <DUPLICATED> but was: <VALID>`.
- **Corrección:** agregar un Set de instancia para guardar los identificadores aceptados y consultar duplicados después de las demás validaciones. Solo se almacenan registros válidos.
- **Estado:** Resuelto. La prueba de duplicados y la de aceptación de un documento diferente pasan.
- **Commits:** RED `95c2e26`, GREEN `03c2bae`, REFACTOR `cd5a00f`.

## Verificación final de las iteraciones 5 y 6

La ejecución de `mvn clean test` después del último refactor terminó con 16 pruebas, 0 fallos, 0 errores y `BUILD SUCCESS`.