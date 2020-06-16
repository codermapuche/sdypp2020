# Trabajopráctico N° 2

#1. Desarrolle una red P2P de carga, búsqueda y descarga de archivos.

Para el desarrollo de este primer punto, se opto por tomar un enfoque mas generico, pensando en reutilizar parte de este desarrollo en futuros puntos e incluso en el TP final.

## p2p

Para lo cual, se realizo una mejora sobre la implementacion propuesta, y es que se elimino la dependencia de un archivo de configuracion, en su lugar, se realizo una division de tareas, por un lado, dentro del paquete p2p se crearon dos clases genericas para la comunicacion en redes p2p, dentro hay dos clases:

### Node
Esta clase representa un nodo de una red p2p, basicamente, un nodo p2p realiza un minimo de 4 tareas en simultaneo:
1. Crea un servidor TCP en el primer puerto efimero disponible y agurda conexiones de otros nodos.
2. Cada un periodo de tiempo configurable, emite un multicast UDP informando el ID del nodo (calctulado en base a una marca de tiempo), el nombre (definido por la implementacion, vease mas adelante) y el puerto del servidor TCP donde esta escuchando.
3. Cada vez que se recibe una conexion de otro peer, el nodo crea un hilo nuevo de si mismo para procesar los mensajes recibidos, cada vez que recibe un mensaje, crea un nuevo hilo para procesar el mensaje de forma independiente, pudiendo atender no solo varios peers en simultaneo, sino tambien varias tareas de un mismo nodo multiplexando el socket.
4. Cada vez que el nodo requiere enviar un mensaje a otro nodo, el envio de dicho mensaje se realiza en un hilo aparte, de manera tal que el envio de datos por la red se realize en segundo plano sin bloquear el hilo principal. La naturaleza no bloqueante que esto impone aporta beneficios como se vera mas adelante.
Los nodos poseen un unico metodo publico de interes, llamdo runIn, el cual permite enviar una tarea a otros peers de la red, internamente, este metodo delega el envio a la Network en la que se encuentre el nodo.
La clase posee dos metodos publicos que pueden y deben ser sobreecritos por las clases que hereden de este, estos son:
1. onStart: Este metodo es algo asi como el hilo principal del nodo, se ejecuta cuando el nodo se conecta a una Network la cual recibe como unico parametro, dentro de este metodo, se debe implmentar toda la logica relacionada con el ciclo de vida del nodo.
2. onMessage: Este metodo se ejecuta cada vez que el nodo recibe un mensaje de otro peer, debe tenerse en cuenta que tal como se menciona en el punto 3 previo, este metodo se ejecuta en su propio hilo y podria haber varios corriendo en simultaneo ademas del onStart, el cual tambien corre en su propio hilo idependinte, no debe asumirse sincronia entre estos metodos. Recibe como parametros el nodo que envio el mensaje, el tipo de nodo de que se trata (esto es el nombre que se menciono antes, y que se explicara mas adelante que es, por ahora veamoslo solo como un string que puede ser "cliente"/"servidor" para el caso de este ejercicio, ya se vera mas en detalle para que sirve y como se define), la tarea que el peer envio mediante el metodo runIn antes explicado y los argumentos de la misma en forma de array de strings.

De forma abstracta, esta clase no hace nada por si sola, pero contiene toda la implementacion de los barebones de un nodo perteneciente a una red p2p generica, permitiendonos luego heredar de esta clase y solo implementar la logica que nos interesa sin preocuparnos por su funcionamento interno, just work.

### Network
Esta clase representa a una red p2p en si misma, podemos pensar que instanciar esta clase es como crear una red p2p imaginaria vacia, a la cual pueden conectarse los nodos, este concepto de red no es un concepto distribuido, es decir, no existe una clase universal y replicada entre los peers, cada peer implementara su propia instancia de esta clase y dichas instancias no tienen ninguna comunicacion entre si.
Esta clase tiene dos funcionalidades principales:
1. Escuchar multicast udp en un puerto configurable recibido por parametro dentro de un hilo independiente.
2. Administrar un pool de sockets entre los nodos y ofrecer sincronia en la escritura sobre los mismos.
Al igual que la clase Node, esta es una clase abstracta que puede y debe ser heredada, aunque a diferencia de Node, es mucho mas "simple".
Solo posee un metodo publico que puede opcionalmente ser sobreecrito, se trata de onDiscover, un metodo que se ejecuta cada vez que la red descubre un nuevo nodo, se puede o no, tomar un accion al detectar este evento.
Al crear una instancia se debe indicar un nombre y un puerto, el puerto se trata del que utilizaran los nodos para enviar sus multicast udp, se utilizan multicast dado que funcionan tanto en red como en el mismo equipo y no bloquean el puerto.
El nombre es simplemente un nombre, un prefijo que se usa para permitir tener varias redes p2p en un mismo puerto y que los nodos no mezclen.
Quiza, lo mas destacable de esta clase (y lo unico que resta por mencionar) es su metodo runFrom, el cual recibe como parametro un Node y la tarea que quiere ejecutar en la red, se trata del metodo en el cual internamente los nodos delegan la tarea de ejectuar runIn, y su poder radica en que puede operar de dos formas:
1. Ejecucion p2p directa: Un nodo envia un mensaje a otro nodo indicando su id, es decir Alice le envia un mensaje a Bob, esto significa que si hay 10 peers, el mensaje de alice solo se envia a Bob mediante un socket privado entre Alice y Bob, si bob no esta disponible el envio falla.
2. Ejecucion p2p anonima: Aca esta la magia de todo, Alice tiene una tarea para derivar en un nodo Electricista, pero no conoce a ninguno, no sabe quienes estan conectados a la red, si es que acaso hay alguno, entonces le indica a la red que envie la tarea a los nodos de nombre "electricista" (este es el nombre del que hablamos antes y que sirve para agrupar nodos segun su clase) siguiendo algun criterio de envio, actualmente se implmentaron 2 criterios:

_FIRST_: Ejecuta la tarea en el primer nodo disponible de la clase indicada.
_ALL_: Ejectuta la tarea en todos los nodos disponibles.
Ambos metodo fallan solo si no hay ningun nodo disponible.
Se considero la implementacion de un nuevo metodo llamado _RR_ que realice un round robin pero la complejidad era significativamente mayor que las dos alternativas anteriores por lo que se deja abierta la posibilidad de implementarlo en un futuro.
Tambien se considero implementar un metodo _RANDOM_ pero se descarto por su falta de utilidad en decantacion con _FIRST_, no se descarta implementarlo en un futuro pero de movida no fue necesario.

## fsnet

Este paquete, contiene la implementacion en si del punto solicitado basandose en las clases previamente descriptas.

### FileSharingNetwork

Esta clase es la mas simple, hereda de p2p.Network y sobre escribe el metodo onDiscover aunque no hace nada cuando ocurre.

### Client

Esta clase hereda de p2p.Node y representa un nodo de la clase cliente, tiene un void main que permite ejecutarlo de forma directa y al arrancar realiza las siguientes operaciones:

1. Crea una instancia de FileSharingNetwork, con el nombre "SDYPP", en la red "224.0.0.1" y puerto 8888 (estos son los datos del multicast udp)
2. Crea una instancia de la clase Client
3. Conecta la instancia de Client a la instancia de la red bajo el nombre de "client".

Luego de realizado esto, internamente las clases del paquete p2p comienzan a crear los hilos necesarios para escuchar los multicast udp y descubir nuevos nodos, se crea el server tpc para recibir conexiones, se difunde el puerto a los demas nodos, y cuando todo este setup esta listo, se llama al metodo onStart de la clase Client, donde, lo que se realizo fue una implementacion de una interface interactiva por consola, se sugiere utilizar el comando "help" a la hora de probarlo para ver las opciones.
Basicamente lo que debemos hacer para que este nodo pueda compartir archivos en la red es lo siguiente:
1. index /home/p2pshare: este comando lo que hace es crear un indice del directorio indicado (el cual debe existir previamente, debe ser cualquier ruta valida). Al finalizar, nos mostrara un id generado del indice, que sera siempre unico y diferente y un listado con los archivos indexados, prestar atencion al id del indice, lo vamos a necesitar.
2. share INDEXID: debemos reemplazar INDEXID por el id resultante del comando anterior, al hacer esto, estaremos enviando el indice al primer nodo master disponible de la red, si no hay ninguno, veremos un error, debemos ir a levantar un nodo master (mas adelante se explica este nodo) y luego repetir el comando.
3. search hola.txt 5: Este comando envia al master la solicitud de buscar el archivo "hola.txt" dentro del indice centralizado, el tercer parametro es un 5, y representa la cantidad de segundos que estamos dispuestos a esperar para que el master nos responda con resultados, es una suerte de timeout, dado que la consulta se envia de forma asincronica a la red, debemos esperar a que esta nos responda.
Se implemento de esta forma, a falta de conocimientos especificos de la herramienta sobre como pausar un hilo hasta que ocurra un evento en otro que corre en paralelo, dado que cuando el thread recibe los resultados podria automaticamente despertar al hilo principal, evitando este timer, se sospecha que es posible de hacer, pero no se logro hacer funcionar y se opto por fix mas rudimentario.
4. download FILEID /home/p2p/download/hola.txt: Este comando, donde FILEID es el ID del archivo deseado del listado recibido, sin los corchetes, y el segundo parametro es la ruta donde queremos almacenarlo, lo que hace es descargar el archivo del nodo que lo posee. Funciona con archivos chicos y con isos de 4Gb+, se implento de manera tal que se realicen transferencia parciales de 10MB y se escriban de forma directa en el disco, lo cual permitiria, en una etapa mas avanzada, quiza como propuesta de tp final, hacer que las descargas se puedan pausar y reanudar, e incluso descargar de varios nodos en simultaneo fragmentos de un mismo archivo, maximizando la velocidad al paralelizar la descarga. estas caracteristicas se proponen como una posible tematica de tp final.

### Master

Esta clase, es practicamente similar al Client, ya que tambien hereda de p2p.Node pero su diferencia radica en el funcionamiento.
Basicamente, podemos hacer 2 cosas:

1. search hola.txt: Busca hola.txt en el indice local, en este caso no se requiere timeout ya que es una busqueda local. Puede probarse a ejecutar este comando antes y despues de ejecutar el share en el cliente.
2. replicate: Este comando no lleva argumentos, siplemente fuerza una replicacion en todos los nodos maestros de la red, la replicacion ocurre de forma automatica al momento que un cliente realiza un share, es decir, que si hay 2 nodos master corriendo y un cliente hace un share al primero, este se replicara en el segundo de forma automatica, pero si aparece un tercero luego, sera necesario llamar a este comando en uno de los dos primeros nodos para que se repliquen en el tercero.
Aca tambien se podria implementar que cuando se descubra un nuevo nodo master, se produzca una replicacion automatica, es una mejora viable de implementar y que podria aplicar como una mejora para el TP final.

No hay mucho mas en el master mas alla de algun detalle minimo del protocolo de implementacion que es irrelevante mencionar.

Dado que este desarrollo servira como base para el punto 3, se propone considerarlo como base para una propuesta de TP final.

## Proceso de depositos/extracciones en banco.

Para desarrollar este punto, se utilizo la base creada anteriormente y los conceptos teoricos vistos en clase, se realizo una ampliacion de los requisitos para ofrecer un proceso de sincronizacion, implementando un mecanismo de tokens para lograr los bloqueos.

Se utilizo como base el paquete p2p antes explicado, y se crearon dos tipos de nodos:

### Management

Esta clase hereda de p2p.Node y representa un nodo bancario centralizado, es quien almacena de forma centralizada los saldos de las cuentas y distribuye los tokens de acceso a los clientes.

Luego de iniciado, admite el comando "status" el cual listara todas las cuentas, sus saldos actuales y los tokens asociados en caso de corresponder.
La comunicacion interna con los clientes se basa en los siguientes mensajes:
1. createAccount: Este mensaje lo envia el cliente para crear una cuenta nueva, la cual se inicializa con saldo=0 y token=null.
2. getAccount: Este mensaje lo envia un cliente cuando quiere bloquear una cuenta de forma distribuida, si la cuenta no tiene un token valido previo, se crea uno valido por 30 segundos y se le envia al cliente.
3. writeAccount: Este mensaje lo envia el cliente para escribir el saldo de una cuenta, dicho mensaje contiene un token asociado a la cuenta el cual debe ser igual al token informado.
4. leaveAccount: Este mensaje lo envia el cliente para informar que ya no necesita el token y libera al acceso a otros clientes, si el cliente no envia este mensaje, el token se invalida solo a los 30 segundos.

### Client

Esta clase hereda de p2p.Node y representa a un cliente del banco.

Esta clase, admite los comandos "crear", "depositar" y "retirar", los cuales interactuan con los metodos del Management antes mencionados.
Cabe destacar, que a fines practicos, los comando "depositar" y "retirar" admiten como parametro un delay expresado en segundos, que introducen un delay en la ejecucion intencional, el cual debe ser menor a 30 segundos, que tiene como objetivo demorar la liberacion del token para poder observar en el Management como se crea el token (mediante "status") y dar tiempo a crear otro cliente que quiera modificar la misma cuenta y ver que es rechazado porque la cuenta se encuntra en uso actualmente.
El cliente valida a la hora de retirar que el saldo sea mayor a cero.

## Red elastica de servicios.

Para desarrollar este punto, se utilizo la base creada anteriormente y los conceptos teoricos vistos en clase, ademas se realizo una mejora en la red p2p permitiendo el mecanismo de ejecucion _ROUND_ROBIN_ con el objetivo de balancear la ejecucion de procesos entre los Workers disponibles.
Se utilizo como base el paquete p2p antes explicado, y se crearon tres tipos de nodos:

### Worker

Esta clase hereda de p2p.Node y representa un nodo trabajador que resuelve una tarea generica, a fines meramente practicos, la tarea solo consiste en esperar un tiempo recibido como parametro.
Los nodos Worker son instanciados de forma automatica por los nodos "Balancer", cuando un Worker se instancia pueden realizar dos acciones:

1. resolve: Mediante este comando recibe una tarea y la resuelve, al iniciar incrementa la carga y al final la disminuye.
2. leave: Mediante este comando el balanceador le indica que cuando termine lo que esta haciendo se suicide porque sus servicios ya no son requeridos.

### Balancer

Esta clase hereda de p2p.Node y representa a un balanceador, recibe tareas de los clientes y gestiona los workers necesarios para llevarlas adelante, si la cantidad de tareas supera los umbrales, crea nuevos workers bajo demanda, si no hay workers disponibles, bufferea la tarea y reintenta ejecutarla automaticamente sin que el cliente se entere siendo esto transparente para el.

### Client

Esta clase hereda de p2p.Node y representa a un cliente, basicamente admite un unico comando "fire" el cual inicia un lote de tareas que tienen una duracion parametrizable.

Para probar este punto, se debe crear una instancia de Balancer y uno o mas Client, luego en cada cliente ejecutar el comando "fire" con los argumentos correspondientes, internamente se hara todo de forma automatica, si se ponen timers lo sufientemente grandes, daran tiempo a ir a la consola del balancer y ejecutar el comando "status" para ver la cantidad de workers y tareas actuales en ejecucion.

## Operador de sobel.

Para este punto, se utilizo el codigo base del punto anterior haciendo modificaciones particulares, por ejemplo en el cliente se implementaron los metodos "localsobel" y "remotesobel" donde el primero realiza el procesamiento en el cliente y el segundo envia la tarea al balanceador.
En el balanceador se implemento el mecanismo necesario para fragmentar una tarea de sobel en multiples subtareas y delegarlas a los workers, los cuales siguen funcionando de forma eleastica.
En los workers se implento el codigo necesario para realizar el operador de sobel.

En las pruebas,con una imagen de 4000x2666 se observan los siguientes resultados:
Local: 10573ms
Remoto: 80573ms
La razon de que remoto tarde 8 veces mas que en local, se debe a que la mayor parte del tiempo se pierde creando nuevos hilos y allocando procesos, en total, se crearon 14 procesos con 1080 hilos, si los workers estuvieran en equipos distintos o si la tarea a realizar requiriera mas procesamiento se notaria una mejora.
Un detalle es que cuando se rearma la imagen, se puede observar una linea entre cada parte, esto sirve para identificar por donde se realizo la fragmentacion de tareas, en total hay 1080 fragmentos en la imagen de ejemplo.