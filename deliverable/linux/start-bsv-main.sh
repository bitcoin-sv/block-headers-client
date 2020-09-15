#!/bin/bash

DATA_FOLDER="headersv-data"
DATA_DB_FOLDER="db"
DATA_LOG_FOLDER="log"

if [ -d "$DATA_FOLDER" ]; then
    echo "/$DATA_FOLDER folder already created."
    if [ -d "$DATA_FOLDER/$DATA_DB_FOLDER" ]; then
            echo "/$DATA_FOLDER/$DATA_DB_FOLDER folder already created."
    else
            mkdir $DATA_FOLDER/$DATA_DB_FOLDER
            echo "/$DATA_FOLDER/$DATA_DB_FOLDER folder created."
    fi
    if [ -d "$DATA_FOLDER/$DATA_LOG_FOLDER" ]; then
                echo "/$DATA_FOLDER/$DATA_LOG_FOLDER folder already created."
    else
                mkdir $DATA_FOLDER/$DATA_LOG_FOLDER
                echo "/$DATA_FOLDER/$DATA_LOG_FOLDER folder created."
    fi
else
    mkdir $DATA_FOLDER
    mkdir $DATA_FOLDER/$DATA_DB_FOLDER
    mkdir $DATA_FOLDER/$DATA_LOG_FOLDER
    echo "/$DATA_FOLDER folder created."
    echo "/$DATA_FOLDER/$DATA_DB_FOLDER folder created."
    echo "/$DATA_FOLDER/$DATA_LOG_FOLDER folder created."
fi

export JAVA_SPRING_PROFILE=docker-bsv-mainnet
docker-compose up -d
echo "HeaderSV client started in the background."
