# Ecurrencies
The goal of the project is to provide asynchronous and horizontally scalable client abstractions over payment gateway APIs, such as [PayPal](https://www.paypal.com/) and similars.

## Message Format
[Protocol Buffers](https://developers.google.com/protocol-buffers/) were chosen as message format because they provide a platform and language neutral way of serializing structured data. Each supported payment gateway API was carefully studied to provide it's own domain layer as a protocol buffer file (normally a *domain.proto* file), it's descriptor (if you need to work with self-describing messages) and a default Scala implementation, witch is used server side.

Clients must download each gateway *domain.proto* file and compile it using the protocol buffer compiler in order to send and receive messages. Officially, Google only provides compilers for C++, Java and Python. But there is support for many other languages via [third party add-ons](https://code.google.com/p/protobuf/wiki/ThirdPartyAddOns).

## Payment Gateways
Currently supported payment gateways:

*   [PagSeguro](https://pagseguro.uol.com.br/)

## Integration Bindings
Currently supported integration bindings:

*   [RabbitMQ](https://www.rabbitmq.com/)
