# Unsplash-Consumer
Prueba de concepto de un servicio que consume del cliente UNSPLASH
(Pendiente de arreglar la redireccion al login al pedir autenticacion)

Solo tenemos un servicio el cual nos retorna por el mapa que generasemos en filter una respuesta acorde a ese filtro.
```
http://localhost:8000/collection/all?filter=FG
```
UPDATE: Antes de la resolucion de la duda hice una resolucion de filtrado que dejo para enriquecer la prueba, es un filtro que está despriorizado con el principal, es decir si existe el primero el segundo no se evalua.
el campo filterMap se usa como mapa clave valor separando las entradas con punto y coma ";" y separando la clave valor con "::"
```
http://localhost:8000/collection/all?filterMap=title::s;cover_photo_id::FG
```
retornando por ejemplo en ese caso la siguiente respuesta 
```
[
  {
    "id": 0,
    "title": "Light Tones",
    "description": null,
    "cover_photo_id": "1G0gFGFVHd4"
  }
]
```

## Desarrollo
El proyecto se ha desarrollado con SpringBoot 2 y Java 11 como se indica en la prueba, y como se indica que se valorara una arquitectura propia de estas tecnologias he optado por crear una applicacion Full Reactive, pues se empezo a dar soporte en la version 2 de springBoot y la mayoria de las funciones Mono-Flux fueron integradas entre java 8 y 11.

Comentare lo mas interesante del desarrollo junto con posibles aplicaciones y cambios:
#### 1 - [CollectionController](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/controller/CollectionController.java): 
Controlador Rest estandar, le he integrado algunas posibles distinciones del tipo de error que cometemos, no mando descripcion de error porque esa informacion terminaria siendo expuesta a cualquiera que nos consuma y pueda iterar hasta encontrar la forma de integrarse.
Aqui solo añadir el uso del método "decodeParam" para que puedan utilizarse espacios y caracteres especiales en los filtrados.
#### 1 - [AuthCallbackController](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/controller/AuthCallbackController.java): 
Controlador Rest que recibelas respuestas de mensajes de codigo de usuario para actualizar el token que se está utilizando, en caso de error simplemente no actualizamos el token, este endpoint recibiria una peticicion una ver arranque el servicio y el cliente nos mande el code, en un posible refresco programado a implementar o en caso de fallo de login en el flujo principal
#### 2 - [CollectionService](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/service/CollectionService.java):
Usamos un flux de la respuesta que obtenemos del cliente para poder de forma paralela y reactiva resolver la creacion de todos los items que mandaremos como respuesta, para crear el mapa pense en optar soluciones ya montadas como las de la libreria de Guava pero como se solicitava uso de las funciones de java monte una version custom de la creacion de este mapa.
Tambien reseñas que el filtro para el Id 0 fue considerado como un id no valido y respuesta del jsonNode en caso de encontrar este valor, es modificable si no queremos este comportamiento.
#### 3 - [FilterQueryParamService](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/service/FilterQueryParamService.java):
Un enumerdado creado como alternativa a otras dos opciones consideradas porque utilizar la opcion de crear a mano los filtros por todos los items del mapa es poco CLEAN y no prueba los conocimientos de las funciones de java, el caso del uso de reflexion es mas cómodo sobre todo si usamos apiFirst (en el caso de se quiera añadir un campo nuevo con subir la version de la aplicacion ya filtraria tambien por ese campo), pero por el contrario la reflexion es muy mala en rendimiento y normalmente solo la uso para testing.
A tener en cuenta en la clase el uso de paralledStream, para que puedan hacerse las evaluaciones en paralelo y el uso de allMatch que hace que si uno de los filtros no lo cumple deje de intentarlo con los filtros restantes.
tambien el metodo "normalize" el cual usamos para eliminar caracteres especiales y evitar la distincion entre mayusculas y minusculas.
#### 4 - [UnsplashMapper](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/service/FilterQueryParamService.java):
Solo tener en cuenta que he utilizado JsonNode porque en la primera version pensaba que se pudiesen filtrar por todos los campos de la respuesta del cliente, pero eso no tendria mucho sentido pues serian distintos los nombres de los filtros de la respuesta del cliente y la nuestra. Solo lo he dejado para evitar procesar toda la request y clear POJOs para ello, pero es susceptible de un cambio de version donde mapeasemos con otro tipo de clase.
#### 5- [SingletonAuthToken](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/util/SingletonAuthToken.java): 
Clase para administrar el token de acceso en caso de que podamos autenticar en caliente, utilizo el patron de diseño Singleton para asegurar que el token es unico en todo el sistema, solo reseñar de la clase la anotacion synchronized la cual utilizamos para evitar problemas de concurrencia.
#### 6- [UnsplashClientService](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/src/main/java/com/gamero/unsplashconsumer/client/UnsplashClientService.java):
Clase para llamar al cliente en cuestion, pensaba hacer que hiciese un sistema de que haga mas llamadas asincronas si el filtrado no reporta suficientes respuestas pero el rate limit de peticiones del cliente es muy bajo y termino haciendo que me rechace las peticiones.

El resto de clase no tienen demasiado interes pero si necesitais conocer algo de ellas estare encantado de explicarlo.

## [Dockerizacion](https://github.com/manueljgq93/Unsplash-Consumer/blob/main/Dockerfile)

El fichero en cuestion es el utilizado para crear la imagen docker de dockerHub, por ahora solo tiene en el proceso usar el jar creado de mvn, exponer el puerto del contenedor 8080 que es el usado dentro del servicio y meterlo en una imagen.

Pendiente de mejoras para el uso de un multistep que haga el build de maven tambien en este contenedor, lo cual ahorra un proceso al desarrollador.

No he usado docker-compose porque no requerimos un grupo de contenedores, habria sido interesante que el cliente fuese una imagen y no un servicio o incluir la integracion con bbdd, caché y demas.

## Mejoras pendientes
###### 1 Solucionar la redireccion al login, la cual con las cookies de usuario es capaz de resolverme el flujo si la llamada de autenticacion se hace desde el navegador;
###### 2 Crear un scheduler que lo que haga es hacer la actualizacion de ese token en un hilo en segundo plano.
###### 3 Uso de multiStep building en el docker image
###### 4 Creacion de un swagger de contrato del api y uso apiFirst dentro de la misma, para no tener que preocuparnos del manteminiento de los POJOs.
###### 5 Posible creacion de una logica de paginado que haga mas dinamico el uso del servicio.
###### 6 Añadir test unitarios o incluso dejar una infraestructura para probar test integrados (añadido test fuera del periodo de la prueba)
###### 7 Al inventar varios filtros, incluirlos en un patron strategy
