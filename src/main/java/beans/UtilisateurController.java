package beans;

import entities.Utilisateur;
import beans.util.JsfUtil;
import beans.util.PaginationHelper;
import entities.Produit;
import java.io.IOException;
import java.util.List;
import java.io.Serializable;
import static java.util.Collections.list;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import beans.commande;
import java.util.Set;

@ManagedBean
public class UtilisateurController implements Serializable {

    private Utilisateur current;
    private DataModel items = null;
    
    @EJB
    private beans.UtilisateurFacade ejbFacade;

    public Utilisateur getCurrent() {
        return current;
    }

    public void setCurrent(Utilisateur current) {
        this.current = current;
    }
    
   

  

   
    
   
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public UtilisateurController() {
    }
    public Boolean verify(String email,String password) {
        Boolean test = false;
       
        
       List<Utilisateur> mylist = ejbFacade.findAll();
        for (Utilisateur utilisateur : mylist) {
            if (email == null ? utilisateur.getEmail() == null : email.equals(utilisateur.getEmail())) {
                if (password == null ? utilisateur.getPassword() == null : password.equals(utilisateur.getPassword())) {
//                    System.out.println("profile verifie");
                    test = true;
                    
                    if("admin".equals(utilisateur.getRole())){
                        
                        goToAdmin();
                        commande.setId(utilisateur.getId());
                        
                        
                    }else if ("user".equals(utilisateur.getRole())) {
                        goToUser();
                        commande.setId(utilisateur.getId());
                        current.setNom("khadija");
                        
                    }
                    
                    
                    return test;
                }
                return test;
            }
            
        }
        
        return test;
    }
  
    
    public Utilisateur getSelected() {
        if (current == null) {
            current = new Utilisateur();
            selectedItemIndex = -1;
        }
        return current;
    }

    private UtilisateurFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Utilisateur) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Utilisateur();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UtilisateurCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Utilisateur) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UtilisateurUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Utilisateur) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UtilisateurDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Utilisateur getUtilisateur(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Utilisateur.class)
    public static class UtilisateurControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UtilisateurController controller = (UtilisateurController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "utilisateurController");
            return controller.getUtilisateur(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Utilisateur) {
                Utilisateur o = (Utilisateur) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Utilisateur.class.getName());
            }
        }

    }
    public void goToAdmin(){
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(ec.getRequestContextPath()+"/faces/admin.xhtml");
        } catch (IOException e) {
        }
        
    }
     public void goToUnkown(){
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(ec.getRequestContextPath()+"/faces/index.xhtml");
        } catch (IOException e) {
        }
        
    }
        public void goToUser(){
             ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(ec.getRequestContextPath()+"/faces/cart.xhtml");
        } catch (IOException e) {
        }
        
    }
    public void login(){
        Boolean tester = verify(current.getEmail(),current.getPassword());
       
        if(!tester){
           
          goToUnkown();
        }
    }

}
