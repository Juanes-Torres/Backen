# Correo de bienvenida (Gmail)

Cuando alguien se registra, KAIRÓS le envía un correo de bienvenida. El envío usa el servidor de correo de Gmail con una **contraseña de aplicación** de Google.

- El envío va **en segundo plano**: el registro responde de inmediato.
- Si el correo falla, **el usuario igual queda registrado**. Solo queda un aviso en el log.
- Si el correo está apagado (`kairos.mail.enabled=false`, el valor por defecto), la app funciona igual y escribe en el log: `Correo deshabilitado: no se envía la bienvenida a ...`.

## Cómo está hecho (arquitectura hexagonal)

| Pieza | Capa | Qué hace |
|---|---|---|
| `NotificacionPort` | application/port/out | Puerto de salida: "avisar al usuario" |
| `RegistrarUsuarioService` | application/usecase | Después de guardar al usuario, llama al puerto |
| `GmailNotificacionAdapter` | infrastructure/adapter/out/notificacion | Envía el correo por Gmail (si `kairos.mail.enabled=true`) |
| `NotificacionDeshabilitadaAdapter` | infrastructure/adapter/out/notificacion | Solo escribe en el log (si el correo está apagado) |

El caso de uso no sabe que existe Gmail. Por eso `RegistrarUsuarioServiceTest` lo prueba con una notificación falsa, sin internet ni base de datos.

## Configurarlo en desarrollo (tu computadora)

1. **Crear la contraseña de aplicación** (mejor con una cuenta de Gmail del proyecto, no la personal):
   1. Entra a `myaccount.google.com` → **Seguridad** → activa la **Verificación en 2 pasos**.
   2. Entra a `myaccount.google.com/apppasswords`, escribe el nombre `KAIROS` y dale a **Crear**.
   3. Copia los **16 caracteres**. Google solo los muestra una vez.
2. **Crear `secrets.properties`** en la raíz del proyecto (junto al `pom.xml`), copiando `secrets.properties.example`:

   ```properties
   kairos.mail.enabled=true
   spring.mail.username=tu_correo@gmail.com
   spring.mail.password=xxxx xxxx xxxx xxxx
   ```

3. Arranca el backend y registra un usuario con un correo real. En el log debe salir `Correo de bienvenida enviado a ...`. Revisa también la carpeta **Spam**.

> `secrets.properties` está en el `.gitignore`: **nunca** se sube a GitHub. No pegues la contraseña de aplicación en el chat ni en otros archivos.

## Pre-producción y producción

Los secretos van en variables de entorno:

| Variable | Obligatoria | Para qué |
|---|---|---|
| `FRONTEND_URL` | Sí | Dirección del frontend, para el botón "Iniciar sesión" del correo |
| `MAIL_ENABLED` | No | `true` para enviar correos (por defecto, apagado) |
| `MAIL_USERNAME` | No | Cuenta de Gmail que envía los correos |
| `MAIL_PASSWORD` | No | Contraseña de aplicación de Google |

`.\scripts\ambiente.ps1 -Ambiente produccion -SoloMostrar` muestra cuáles están configuradas (los secretos aparecen ocultos).
