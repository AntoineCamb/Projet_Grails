package fr.mbds.tp

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

@Secured('ROLE_ADMIN')
class RoleController {

    RoleService roleService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond roleService.list(params), model:[roleCount: roleService.count()]
    }

    def show(Long id) {
        def roleInstance = Role.get(id)
        def userRoleList = UserRole.findAllByRole(roleInstance)
        def userList = userRoleList.collect{ it.user }

        respond roleService.get(id), model: [role:roleInstance, userList:userList]
    }

    def create() {
        def userList = User.findAll()
        respond new Role(params)
    }

    def save(Role role) {
        if (role == null) {
            notFound()
            return
        }

        try {
            roleService.save(role)
        } catch (ValidationException e) {
            respond role.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'role.label', default: 'Role'), role.id])
                redirect role
            }
            '*' { respond role, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond roleService.get(id)
    }

    def update(Role role) {
        if (role == null) {
            notFound()
            return
        }

        try {
            roleService.save(role)
        } catch (ValidationException e) {
            respond role.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = "Le rôle a été mis à jour"
                redirect role
            }
            '*'{ respond role, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        roleService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = "Le rôle a bien été supprimé"
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = "Le role n'a pas été trouvé"
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
