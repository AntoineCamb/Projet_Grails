package tp

import fr.mbds.tp.User
import fr.mbds.tp.Message
import fr.mbds.tp.UserMessage
import fr.mbds.tp.Role
import fr.mbds.tp.UserRole

class BootStrap {

    def init = { servletContext ->
        def userAdmin = new User(username:"admin", password:"secret", firstName:"admin", lastName:"admin", mail:"admin", isDeleted:false).save(flush:true,failOnError:true)
        def roleAdmin = new Role(authority:"ROLE_ADMIN").save(flush:true,failOnError:true)
        UserRole.create(userAdmin,roleAdmin,true)

        def userDeleted = new User(username: "userDeleted", password: "passwordDeleted", firstName: "userDeleted", lastName:"deletedUser", mail:"user@deleted.com").save(flush:true, failOnError: true)

        def groupEveryone = new Role(authority: "groupEveryone").save(flush: true, failOnError: true)

        (1..50).each{
            def userInstance = new User(username:"username-$it",password:"password",firstName:"first",lastName:"last",mail:"mail-$it", isDeleted:false).save(flush:true,failOnError:true)
            UserRole.create(userInstance, groupEveryone, true)
            new Message(messageContent:"Message de test",author:userInstance).save(flush:true, failOnError:true)
            UserRole.create(userInstance,groupEveryone,true)
        }
       Message.list().each{
           Message messageInstance ->
               User.list().each{
                   User userInstance ->
                       UserMessage.create(userInstance,messageInstance,true)
               }

       }
        def roleInstance = new Role(nomrole:"Pas de rôle").save(flush:true)
        def roleInstance2 = new Role(nomrole:"Administrateur").save(flush:true)


    }
    def destroy = {
    }
}
