# SwiftSearch
## Introduction


<img src=".github/search.png?raw=true" height="350">

SwiftSearch is a simple search engine that implements:
- Crawler
  - Web fetcher
  - Selenium fetcher
- Indexer
- Query engine
  - Cosine similarity
  - PageRank
- Web interface

## System architecture

<img src=".github/architecture.png?raw=true" height="250">

The information retrieval system is based on microservice architecture. Components are independent programs that reside on the same/different server. They communicate with each other using well-defined APIs.
- Crawler fetches and stores web pages in the database
- Indexer fetches the crawled web pages from the crawler and then builds and updates its index
- Query engine uses index built by the indexer to respond to queries
- Web servers provide a web interface to users and forward the request to the query engine.

## Installation

### Compilation guide

1. Make sure [`JDK`](https://www.oracle.com/java/technologies/downloads/) (17+) and [`MongoDB`](https://www.mongodb.com/try/download/community) are installed.

2. If you are on Linux/MacOS, you can directly execute the compile script.

    ```bash
    $ chmod +x ./build.sh
    $ ./build.sh
    ```

    Otherwise, you need to compile everything manual by following the commands provided. 
    1. Compile the `common` module, which contains shared code for different system components.

        ```bash
        cd common
        ./mvnw clean install
        cd ..
        ```

    2. Compile and package the crawler, indexer, query engine, and web server. You can directly run the following script to compile all components

        ```bash
        # Create a directory for executables
        mkdir executables

        # Crawler
        cd crawler
        ./mvnw clean package
        cp ./target/crawler-*.jar ../executables
        cd ..

        # Indexer
        cd indexer
        ./mvnw clean package
        cp ./target/indexer-*.jar ../executables
        cd ..

        # Query engine
        cd query
        ./mvnw clean package
        cp ./target/query-*.jar ../executables
        cd ..

        # Web server
        cd web
        cp ./target/web-*.jar ../executables
        ./mvnw clean package
        cd ..
        ```

## Usage
### Start a crawler task
1. On the crawler server, start a MongoDB instance.

    ```bash
    $ ./mongodb/bin/mongod --dbpath <YOUR_DB_PATH>
    ```

1. Start the application

    ```bash
    # Choose one of the two commands to run the application
    # Option 1:
    $ java -jar ./executables/crawler-*.jar

    # Option 2:
    $ cd crawler
    $ ./mvnw spring-boot:run
    ```

1. Start a crawler task using `curl`.

    The following command starts a crawler task from `https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm` and fetches all pages. Note that this test page does not have outlinks. In practice, you may want to specify an "access control list," which restricts the pages the crawler will access. Additionally, specify the `maxDepth` and `maxVisits` to prevent the crawler from infinitely visiting the Internet. 

    ```bash
    $ curl --location 'http://<CRAWLER_IP_ADDR>:8621/api/v1/task' \
    --header 'Content-Type: application/json' \
    --header 'Cookie: lang=en-US' \
    --data '{
        "taskName": "CSE",
        "baseUrl": "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm",
        "acl": [
            "(https|http)://[a-zA-Z0-9_\\-.]*hkust.edu.hk/(.)*",
            "(https|http)://[a-zA-Z0-9_\\-.]*ust.hk/(.)*"
        ],
        "maxDepth": null,
        "maxVisits": null,
        "fetcher": "default"
    }'
    ```

    `<CRAWLER_IP_ADDR>` should be replaced with the crawler's IP address. If you are accessing locally, it is `127.0.0.1`. Otherwise, if you wish to control it remotely, you may need to configure the firewall and replace `<CRAWLER_IP_ADDR>` with the crawler's IP address.

2. Then, you will get a response like this

    ```json
    {
        "taskId": "6453e6ea4b528518a49cf202",
        ...
    }
    ```

    The `taskId` is an identifier for the task. You will need it for the indexer to build indexes.

## Starting an indexer task

1. On the indexer server, start a MongoDB instance.

    ```bash
    $ ./mongodb/bin/mongod --dbpath <YOUR_DB_PATH>
    ```

1. Start the application

    ```bash
    # Choose one of the two commands to run the application
    # Option 1:
    $ java -jar ./executables/indexer-*.jar

    # Option 2:
    $ cd indexer
    $ ./mvnw spring-boot:run
    ```

1. Start an indexer task

    The following task fetches fetched pages from the crawler and builds an index based on cosine similarity and PageRank.

    ```bash
    curl --location 'http://<INDEXER_IP_ADDR>:8622/api/v1/task/index' \
    --header 'Content-Type: application/json' \
    --header 'Cookie: lang=en-US' \
    --data '{
        "crawlerServerProtocol": "http",
        "crawlerServerAddr": "<CRAWLER_IP_ADDR>:8621",
        "crawlerTaskId": "<TASK_ID>",
        "forceUpdate": true,
        "updateIDF": true,
        "updatePageRank": true
    }'
    ```

    Replace `<TASK_ID>` with a crawler task ID you got from creating crawler tasks. You can find it in the response of crawler creation. In this case, it is `6453e6ea4b528518a49cf202`.

    Replace `<INDEXER_IP_ADDR>` with the **indexer**'s IP address. If you are controlling it locally, it is `127.0.0.1`.

    Replace `<CRAWLER_IP_ADDR>` with the **crawler**'s IP address. If your indexer server and crawler server reside on the same machine, it is `127.0.0.1`.

## Starting a query engine

Usually, the query engine and indexer should reside on the same server. But they can also reside on two separate servers. In that case, you need to ensure they have a high-speed connection so that the query engine has fast access to the MongoDB database, which contains the index.

At this stage, the crawler and indexer are no longer needed. You can terminate them to save computing resources.

1. If the indexer and query engine reside on different server, configure `query/src/main/resources/application.properties`

    ```properties
    spring.data.mongodb.host=<MONGO_DB_ADDR>
    spring.data.mongodb.port=<MONGO_DB_PORT>
    ```

1. Start the query engine

    ```bash
    # Choose one of the two commands to run the application
    # Option 1:
    $ java -jar ./executables/query-*.jar

    # Option 2:
    $ cd query
    $ ./mvnw spring-boot:run
    ```

1. Make a test query

    ```bash
    curl --location 'http://<QUERT_SERVER_ADDR>:8620/api/v1/search?page=1&query=movie'
    ```

    Replace `<QUERT_SERVER_ADDR>` with the **query engine**'s IP address. If you are controlling it locally, it is `127.0.0.1`.

### Starting a web server

The web server uses the query engine's API and responds to user requests.

1. Start the query engine

    ```bash
    $ cd web
    $ ./mvnw spring-boot:run
    ```

1. Open a browser, access `http://<WEB_SERVER_ADDR>:8610` to perform searches using the web interface.

    Replace `<WEB_SERVER_ADDR>` with the **web server**'s IP address. If you are accessing it locally, it is `127.0.0.1`.