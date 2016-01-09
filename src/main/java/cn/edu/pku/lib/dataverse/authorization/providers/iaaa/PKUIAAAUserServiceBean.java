/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.pku.lib.dataverse.authorization.providers.iaaa;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author luopc
 * @version 1.0
 */
@Stateless
public class PKUIAAAUserServiceBean {
    
    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;
    
    public PKUIAAAUser findByUserName(String logonID){
        List<PKUIAAAUser> users = em.createNamedQuery("PKUIAAAUser.findByUserName")
                    .setParameter("userName", logonID).getResultList();
        if(users.isEmpty())
            return null;
        else
            return users.get(0);
    }
    
    public PKUIAAAUser findByEmail(String email){
        List<PKUIAAAUser> users = em.createNamedQuery("PKUIAAAUser.findByEmail")
                    .setParameter("email", email).getResultList();
        if(users.isEmpty())
            return null;
        else
            return users.get(0);
    }
    
    public PKUIAAAUser save(PKUIAAAUser user) {
        if ( user.getId() == null ) {
            // see that the username is unique
            if ( em.createNamedQuery("PKUIAAAUser.findByUserName")
                    .setParameter("userName", user.getUserName()).getResultList().size() > 0 ) {
                throw new IllegalArgumentException( "IAAA user with logon id '" + user.getUserName()+ "' already exists.");
            }
            em.persist( user );
            return user;
        } else {
            return em.merge(user);
        }
    }
}
