CowBrow
=======

CowBrow is a jms queue browser. At the moment probably only openmq/glassfish jms is supported.

CowBrow has both a single command interface and a session interface. The same actions are availble in both:  
```list```: lists queues on the specified host  
```create```: creates a queue  
```destroy```: removes a queue  
```sendtext```: sends a text message to the specified queue  
```listmessages```: list messages on the specified queue (without removing them)  

```java -jar cowbrow.jar -h``` for further help  

###examples:
```
java -jar cowbrow.jar list -H myhost
# output:
    jmsQueue1: queue, 6 consumers, 4 messages (100000 max), 108592 bytes (10737418240max)
    jmsQueue2: queue, 2 consumers, 1 messages (1000000 max), 34433 bytes (0max)
    mq.sys.dmq: queue, 1 consumers, 0 messages (1000 max), 0 bytes (10485760max)
 
java -jar cowbrow.jar cli -H myhost
>create -q newqueue
>sendtext message1 -q newqueue
>sendtext message2 -q newqueue
>listmessages -q newqueue
# output:
Timestamp    Type    Body

2017/03/30 - 08:59:28    TextMessage    message1
2017/03/30 - 08:59:46    TextMessage    message2
```
