package fr.mbds.tp

import grails.converters.JSON
import grails.converters.XML

import javax.servlet.http.HttpServletRequest

class ApiController {

    def index() { render "ok"}

    def message()
    {
        switch (request.getMethod())
        {
            case "GET":
                if(params.id) // si on a un id, on doit retourner une instance de message
                {
                    def messageInstance = Message.get(params.id)
                    if (messageInstance)
                        responseFormat(messageInstance, request)
                    else
                        response.status = 404
                } else  // si pas d'id, on doit retourner une liste de tous les messages
                    forward action:"messages"
                break
            case "POST":
                forward action:"messages"
                break
            case "PUT":
                def messageInstance = params.id ? Message.get(params.id) : null
                if (messageInstance)
                {
                    if (params.get("author.id"))
                    {
                        def authorInstance = User.get(params.get("author.id"))
                        if (authorInstance)
                            messageInstance.author = authorInstance
                    }
                    if (params.messageContent)
                        messageInstance.messageContent = params.messageContent
                    if (messageInstance.save(flush:true)) {
                        response.status = 200
                        return response.status
                    }
                    else
                    {
                        response.status = 400
                        return response.status
                    }
                }
                else
                {
                    response.status = 404
                    return response.status
                }
                break
            case "DELETE":
                def messageInstance = params.id ? Message.get(params.id) : null
                if (messageInstance)
                {
                    // On récupère la liste des UserMessages qui référencent le message que nous souhaitons effacer
                    def userMessages = UserMessage.findAllByMessage(messageInstance)
                    // On itère sur la liste et efface chaque référence
                    userMessages.each{
                        UserMessage userMessage -> userMessage.delete(flush:true)
                    }
                    messageInstance.delete(flush:true)
                    response.status = 200
                    return response.status

                }
                else {
                    response.status = 404
                    return response.status
                }
                break
            default:
                response.status = 405
        }
    }

    def messages()
    {
        switch (request.getMethod())
        {
            case "GET":
                responseFormatList(Message.list(),request)
                break
            case "POST":
                //Vérifier auteur
                def authorInstance = params.author.id ? User.get(params.author.id) : null
                def messageInstance
                if (authorInstance)
                {
                    //Créer le message
                    messageInstance = new Message(author : authorInstance, messageContent : params.messageContent)
                    if(messageInstance.save(flush:true))
                        response.status=201
                        return response.status
                }


                if (response.status !=201)
                    response.status = 400
                break
        }
    }

    def user()
    {
        switch (request.getMethod())
        {
            case "GET":
                if(params.id) // si on a un id, on doit retourner une instance de user
                {
                    def userInstance = User.get(params.id)
                    if (!userInstance.isDeleted){
                        if (userInstance)
                            responseFormat(userInstance, request)
                        else
                            response.status = 404
                    }
                    else
                        response.status = 404
                } else  // si pas d'id, on doit retourner une liste de tous les utilisateurs
                    forward action:"users"
                break
            case "POST":
                forward action:"users"
                break
            case "PUT":
                def userInstance = params.id ? User.get(params.id) : null
                if (userInstance)
                {
                    if (params.username)
                        userInstance.username = params.username
                    if (params.password)
                        userInstance.password = params.password
                    if (params.firstName)
                        userInstance.firstName = params.firstName
                    if (params.lastName)
                        userInstance.lastName = params.lastName
                    if (params.mail)
                        userInstance.mail = params.mail
                    if (userInstance.save(flush:true)) {
                        response.status = 200
                        return response.status
                    }
                    else
                    {
                        response.status = 400
                        return response.status
                    }
                }
                else
                {
                    response.status = 404
                    return response.status
                }
                break
            case "DELETE":
                def userInstance = params.id ? User.get(params.id) : null
                if (userInstance)
                {
                    userInstance.isDeleted = true
                    userInstance.save(flush:true)
                    response.status = 200
                    return response.status

                }
                else {
                    response.status = 404
                    return response.status
                }
                break
            default:
                response.status = 405
        }
    }

    def users()
    {
        switch (request.getMethod())
        {
            case "GET":
                responseFormatList(User.findAllByIsDeleted(false),request)
                break
            case "POST":
                def userInstance
                userInstance = new User(username : params.username, password : params.password, firstName : params.firstName, lastName : params.lastName, mail : params.mail)
                if(userInstance.save(flush:true))
                    response.status=201
                return response.status
                if (response.status !=201)
                    response.status = 400
                break
        }
    }
    def responseFormat(Object instance, HttpServletRequest request)
    {
        switch(request.getHeader("Accept"))
        {
            case "text/xml":
                render instance as XML
                break
            case "text/json":
                render instance as JSON
                break
            default:
                response.status = 415
                break
        }
    }
    def responseFormatList(List list, HttpServletRequest request)
    {
        switch(request.getHeader("Accept"))
        {
            case "text/xml":
                render list as XML
                break
            case "text/json":
                render list as JSON
                break
        }
    }
    def getListActualisee (List list){

    }
}

