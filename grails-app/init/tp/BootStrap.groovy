package tp

import fr.mbds.tp.User
import fr.mbds.tp.Message
import fr.mbds.tp.UserMessage
import fr.mbds.tp.Role
import fr.mbds.tp.UserRole

class BootStrap {

    def init = { servletContext ->
        def userAdmin = new User(username:"admin", password:"secret", firstName:"admin", lastName:"admin", mail:"admin").save(flush:true,failOnError:true)
        def roleAdmin = new Role(authority:"ROLE_ADMIN").save(flush:true,failOnError:true)
        UserRole.create(userAdmin,roleAdmin,true)
        (1..50).each{
            def userInstance = new User(username:"username-$it",password:"password",firstName:"first",lastName:"last",mail:"mail-$it").save(flush:true,failOnError:true)

            new Message(messageContent:"lala",author:userInstance).save(flush:true, failOnError:true)
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
