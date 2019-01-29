package fr.mbds.tp

import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityService
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

@Secured('ROLE_ADMIN')
class MessageController {

    MessageService messageService

    SpringSecurityService springSecurityService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond messageService.list(params), model:[messageCount: messageService.count()]
    }

    def show(Long id) {
        def messageInstance = Message.get(id)
        def userMessageList = UserMessage.findAllByMessage(messageInstance)
        def userList = userMessageList.collect{it.user}

        respond messageInstance, model: [userList:userList]
    }

    def create() {
        respond new Message(params)
        def userList = User.findAll()
        def roleList = Role.findAll()

        respond new Message(params), model: [userList: userList, roleList: roleList]
    }

    def save(Message message) {
        if (message == null) {
            notFound()
            return
        }

        try {
            messageService.save(message)
        } catch (ValidationException e) {
            respond message.errors, view:'create'
            return
        }

        if (params.get("destinataires")) {
            def destinatairesList = User.getAll(params.list("destinataires"))
            destinatairesList.each {
                new UserMessage(user: it, message: message).save(flush: true)
            }
        }

        if (params.get("groupedestinataire")) {
            def groupeList = Role.getAll(params.list("groupedestinataire"))
            groupeList.each {
                new UserMessage(user:it, message:message).save(flush:true)
            }
        }

        request.withFormat {
            form multipartForm {
                flash.message = "Le message a été correctement créé"
                redirect message
            }
            '*' { respond message, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond messageService.get(id)
    }

    def update(Message message) {
        if (message == null) {
            notFound()
        return
        }

        try {
            messageService.save(message)
        } catch (ValidationException e) {
            respond message.errors, view:'edit'
            return
        }

        // Récupérer l'id du destinataire
        // Instancier ce dernier
        def idest = User.get(params.receiver)
        // Créer une instance de UserMessage correspondant à l'envoi de ce message
        UserMessage.create(idest, message, true)
        // Persister l'instance UserMessage créée

        // Si groupe spécifié :
        // Récupérer l'instance de Rôle désigné
        // Créer un nouveau userMessage pour tous les utilisateurs du groupe

        request.withFormat {
            form multipartForm {
                flash.message = "Le message ayant l'id ${message.id} a été correctement mis à jour"
                redirect message
            }
            '*'{ respond message, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        messageService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = "Le message avec l'id ${message.id} a bien été supprimé"
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = "Le message avec l'id ${message.id} n'a pas été trouvé"
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
