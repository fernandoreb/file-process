package com.redhat.kafka.fileprocessor;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class APDataFileProcessRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /* 
         * Documentações dos componentes
         * 
         * Componente FTP
         * https://camel.apache.org/components/next/ftp-component.html
         * 
         * Componente Kafka
         * https://camel.apache.org/components/next/kafka-component.html
         * 
         * Componente File
         * https://camel.apache.org/components/next/file-component.html
         */
                
        //rota: ftp pasta processar -> posta conteúdo no kafka -> move arquivo para pasta processado no ftp
        from("{{ftp.component}}://{{ftp.user}}@{{ftp.server}}:{{ftp.port}}/{{ftp.directory.toprocess}}?"+ //1 - leitura de arquivos do ftp
              "password={{ftp.password}}&"+
              "knownHostsFile={{ftp.known_hosts}}&strictHostKeyChecking={{ftp.strictHostKeyChecking}}&"+
              "scheduler=quartz&scheduler.cron={{scheduler.cron.expression}}&"+ // uso do componente scheduler para agendamento via cron
              "move={{ftp.directory.move}}") // 3 - esta opção moverá o arquivo quando a rota finalizar.
              .routeId("apdata-file-process-kafka")
              .log("apdata-file-process - envio kafka iniciado")
              .log("apdata-file-process - envio kafka arquivo a processar: ${header.CamelFileName}")
              .log("apdata-file-process - envio kafka enviando para tópico: {{topic.name.producer}}")
              .to("kafka:{{topic.name.producer}}?brokers={{kafka.bootstrap-servers}}" + //2 - envio para tópico do kafka
              "&securityProtocol={{kafka.security.protocol}}" +
              "&saslMechanism={{kafka.producer.properties.sasl.mechanism}}" +
              "&sslTruststoreLocation={{kafka.producer.ssl.trust-store-location}}" +
              "&sslTruststorePassword={{kafka.producer.ssl.trust-store-password}}" +
              "&sslTruststoreType={{kafka.producer.ssl.trust-store-type}}" +
              "&saslJaasConfig={{kafka.producer.properties.sasl.jaas.config}}")
              .log("apdata-file-process - envio kafka finalizado");

        //rota: ftp pasta processado -> download para diretório -> remove arquivo da pasta processado do ftp     
        from("{{ftp.component}}://{{ftp.user}}@{{ftp.server}}:{{ftp.port}}/{{ftp.directory.processed}}?"+ //1 - leitura de arquivos do ftp
              "password={{ftp.password}}&"+
              "knownHostsFile={{ftp.known_hosts}}&strictHostKeyChecking={{ftp.strictHostKeyChecking}}&"+
              "scheduler=quartz&scheduler.cron={{scheduler.cron.expression}}&"+ // uso do componente scheduler para agendamento via cron
              "delete=true") // 3 - esta opção removerá o arquivo quando a rota finalizar.
              .routeId("apdata-file-process-fileserver")
              .log("apdata-file-process - envio fileserver iniciado")
              .log("apdata-file-process - envio fileserver arquivo a processar: ${header.CamelFileName}")
              .log("apdata-file-process - envio fileserver enviando arquivo ${header.CamelFileName} para diretório: {{file.directory}}")
              .to("file:{{file.directory}}") // 2 - envio do arquivo para um diretório 
              .log("apdata-file-process - envio fileserver arquivo processado: ${header.CamelFileName}") 
              .log("apdata-file-process - envio fileserver finalizado");
        
       }

}