package com.fhj.byteparse

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

fun main() {

}

fun getData(){
    HttpClient {
        this.install(ContentNegotiation){

        }
    }
}