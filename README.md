# **DeluxeClans** 
Es un plugin ligero y gratuito para servidores de Minecraft que permite a los jugadores crear, unirse y gestionar clanes de manera sencilla.  

Creado por **Vediikaz** | Soporte: [Discord](https://discord.gg/VUxkyXzXqN)

---

## Características

- Crear clanes con nombre personalizado.
- Unirse a clanes existentes (si está habilitado en la config).
- Salir o disolver clanes.
- Ver información detallada del clan (dueño, miembros, fecha de creación).
- Menú gráfico interactivo para gestionar clanes.
- Configuración de longitud de nombre y límite de clanes por jugador.
- Mensajes personalizables con colores y placeholders.
- Temporizador y barra de progreso al crear un clan.

---

## Comandos

Todos los comandos usan el prefijo `/clan`:

| Comando | Descripción |
|---------|-------------|
| `/clan crear <nombre>` | Crear un nuevo clan. |
| `/clan unirse <nombre>` | Solicitar unirse a un clan existente. |
| `/clan salir` | Salir del clan actual. |
| `/clan disolver` | Disolver tu clan (si eres el dueño). |
| `/clan info` | Ver información de tu clan. |
| `/clan menu` | Abrir el menú gráfico de clanes. |
| `/clan ayuda` | Mostrar esta lista de comandos. |

---

## Configuración (`config.yml`)

```
prefix: "&6[&fDeluxe&l&3Clans&6] &r"

options:
  maxClanNameLength: 20
  minClanNameLength: 3
  maxClansPerPlayer: 1
  allowClanJoinRequests: true
  clanTagFormat: "[{tag}]"
messages:
  reloaded: "&a✔ Configuración recargada correctamente."
  noPermission: "&c✖ No tienes permiso para hacer esto."
  usage: "&eUso correcto: &f/clan &7<crear|unirse|salir|disolver|info|ayuda>"
  noClanName: "&c✖ Debes especificar un nombre de clan."
  invalidClanLength: "&c✖ El nombre del clan no cumple con la longitud permitida."
  alreadyHasClan: "&c✖ Ya perteneces a un clan."
  clanCreated: "&a✔ ¡Has creado tu clan con éxito!"
  joinDisabled: "&c✖ Unirse a clanes está deshabilitado."
  clanJoined: "&a✔ Te has unido al clan &f{clan}&a correctamente."
  clanLeft: "&e➜ Has salido de tu clan."
  clanDisbanded: "&c✖ Has disuelto tu clan."
  noClanAssigned: "&c✖ No perteneces a ningún clan."
  invalidSubcommand: "&c✖ Subcomando inválido. Usa &e/clan ayuda"
  errorLoadingClans: "&c✖ Error cargando los datos de los clanes."
  errorCreatingClan: "&c✖ Ha ocurrido un error al crear el clan."
  clanInfo: |
    {prefix}
    &b&lInformación del Clan
    &7Nombre: &f{clan}
    &7Dueño: &f{owner}
    &7Creado: &f{created}
    &7Miembros &f({members}): &a{member_list}
    {prefix}
```

Puedes personalizar los mensajes y las opciones según tu servidor. Recuerda reiniciar el servidor o usar /reload para aplicar los cambios.

## Instalación
- Coloca DeluxeClans.jar en la carpeta plugins de tu servidor.
- Inicia el servidor para que se genere la configuración por defecto.
- Configura config.yml según tus preferencias.
- Reinicia el servidor o usa /reload.

¡Listo! Los jugadores ya pueden usar /clan.
