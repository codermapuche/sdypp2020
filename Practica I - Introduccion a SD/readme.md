# Practica I - Introduccion a SD

Para instalar los proyectos, se debe seguir estos pasos:

1. Crear un nuevo proyecto
2. Crear un package llamado "sdypp"
3. Importar en dicho package desde "FileSystem" los archivos publicados.


## 1

En Java, la ejecucion es sincronica, esto significa que cada linea de codigo se
ejecuta siempre despues de la que le precede y antes de la que le sigue, por 
este motivo, al crear un servidor de socket, la sentencia de asignacion de un 
nuevo socket detiene la ejecucion del programa hasta que aparezca un cliente para
completar dicha asignacion, a su vez, tambien causa que atender a un segundo cliente
requiera esperar a que termine el procesamiento del anterior.

## 2
Al usar Workers para mejorar el primer punto, el proceso Master del servidor puede
atender multiples conexiones sin interrupciones ya que delega en los Workers la tarea 
de realizar el procesamiento. Esto si bien es correcto a nivel teorico, en la practica
esta limitado por la cantidad de cores disponibles en el hardware, escribir codigo pensado
para utilizar multiples cores de forma dinamica aporta la ventaja de que si el software
se ejecuta en equipos de mejores prestaciones podra hacer un uso optimo de los mismos.

## 3
En este punto atento a las correcciones se implemento una interface interactiva basada en comandos
los cuales se muestran al iniciar el cliente, basicamente si alice quiere dejarle un mensaje a bob puede 
hacerlo escribiendolo en su buzon:

```
write @ bob @ Hello bob, i'am alice, how are you?
```

De esta forma, alice deja un mensaje anonimo en la cola de "bob", no obstante esto podria ser mas especifico:

```
write @ bob.alice @ Hello bob, how are you?
```

En este caso, el mensaje se escribe en la cola "bob.alice" indicando el remitente.
Bob, por su parte puede leer los mensajes "anonimos" haciendo:

```
read @ bob
```

O bien leer solo los de alice, haciendo:

```
read @ bob.alice
```

Los criterios de autenticacion de usuarios quedan fuera del alcance de la implementacion.
Por ejemplo, bob para saber que tiene que revisar mensajes de alice tendria que conocer previamente su existencia.
Para esto alice podria hacer:

```
write @ bob @ Hello bob, i'am <alice>, i sended a private message to you! 
write @ bob.alice @ Dear bob, i'm in love with you.
```

Por supuesto que al no haber autenticacion todo esto no es confiable, y posiblemente alice sea un nigeriano interesado en los datos bancarios de bob, por lo que validaciones adicionales son deseables.

Esto puede lograrse en terminos sistemicos implementando un protocolo para la comunicacion.

# 4

Se agrego la posibilidad de vaciar los mensajes mediante el comando clean.

```
clean @ bob
```

# 6

En RMI los parametros se pasan mediante la interface Serializable, esto implica que las propiedades
serializables se mantendran sincronizadas a ambos lados, pero aquellas que no lo sean se perderan, si alguna propiedad que no sea serializable se utiliza para el proceso, el mismo podria resultar en datos inconsistentes dependiendo de quien lo ejecute.

# 7

En este punto, se implemento una interface Tarea, para probarlo se requiere ejecutar primero el server y luego el cliente, este ultimo genera 3 tareas diferentes y las envia a ejecutar al server el cual las procesa correctamente de forma secuencial.