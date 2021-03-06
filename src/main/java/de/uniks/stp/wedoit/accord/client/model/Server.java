package de.uniks.stp.wedoit.accord.client.model;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class Server {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_OWNER = "owner";
    public static final String PROPERTY_CATEGORIES = "categories";
    public static final String PROPERTY_MEMBERS = "members";
    public static final String PROPERTY_LOCAL_USER = "localUser";
    public static final String PROPERTY_INVITATIONS = "invitations";
    public static final String PROPERTY_REFERENCE_MESSAGE = "referenceMessage";
    protected PropertyChangeSupport listeners;
    private String name;
    private String id;
    private String owner;
    private List<Category> categories;
    private List<User> members;
    private LocalUser localUser;
    private List<Invitation> invitations;
    private String referenceMessage;

    public String getName()
   {
      return this.name;
   }

    public Server setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_NAME, oldValue, value);
      return this;
   }

    public String getId()
   {
      return this.id;
   }

    public Server setId(String value)
   {
      if (Objects.equals(value, this.id))
      {
         return this;
      }

      final String oldValue = this.id;
      this.id = value;
      this.firePropertyChange(PROPERTY_ID, oldValue, value);
      return this;
   }

    public String getOwner()
   {
      return this.owner;
   }

    public Server setOwner(String value)
   {
      if (Objects.equals(value, this.owner))
      {
         return this;
      }

      final String oldValue = this.owner;
      this.owner = value;
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

    public List<Category> getCategories()
   {
      return this.categories != null ? Collections.unmodifiableList(this.categories) : Collections.emptyList();
   }

    public Server withCategories(Category value)
   {
      if (this.categories == null)
      {
         this.categories = new ArrayList<>();
      }
      if (!this.categories.contains(value))
      {
         this.categories.add(value);
         value.setServer(this);
         this.firePropertyChange(PROPERTY_CATEGORIES, null, value);
      }
      return this;
   }

    public Server withCategories(Category... value)
   {
      for (final Category item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

    public Server withCategories(Collection<? extends Category> value)
   {
      for (final Category item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

    public Server withoutCategories(Category value)
   {
      if (this.categories != null && this.categories.remove(value))
      {
         value.setServer(null);
         this.firePropertyChange(PROPERTY_CATEGORIES, value, null);
      }
      return this;
   }

    public Server withoutCategories(Category... value)
   {
      for (final Category item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

    public Server withoutCategories(Collection<? extends Category> value)
   {
      for (final Category item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

    public List<User> getMembers()
   {
      return this.members != null ? Collections.unmodifiableList(this.members) : Collections.emptyList();
   }

    public Server withMembers(User value)
   {
      if (this.members == null)
      {
         this.members = new ArrayList<>();
      }
      if (!this.members.contains(value))
      {
         this.members.add(value);
         value.withServers(this);
         this.firePropertyChange(PROPERTY_MEMBERS, null, value);
      }
      return this;
   }

    public Server withMembers(User... value)
   {
      for (final User item : value)
      {
         this.withMembers(item);
      }
      return this;
   }

    public Server withMembers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withMembers(item);
      }
      return this;
   }

    public Server withoutMembers(User value)
   {
      if (this.members != null && this.members.remove(value))
      {
         value.withoutServers(this);
         this.firePropertyChange(PROPERTY_MEMBERS, value, null);
      }
      return this;
   }

    public Server withoutMembers(User... value)
   {
      for (final User item : value)
      {
         this.withoutMembers(item);
      }
      return this;
   }

    public Server withoutMembers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutMembers(item);
      }
      return this;
   }

    public LocalUser getLocalUser()
   {
      return this.localUser;
   }

    public Server setLocalUser(LocalUser value)
   {
      if (this.localUser == value)
      {
         return this;
      }

      final LocalUser oldValue = this.localUser;
      if (this.localUser != null)
      {
         this.localUser = null;
         oldValue.withoutServers(this);
      }
      this.localUser = value;
      if (value != null)
      {
         value.withServers(this);
      }
      this.firePropertyChange(PROPERTY_LOCAL_USER, oldValue, value);
      return this;
   }

    public List<Invitation> getInvitations()
   {
      return this.invitations != null ? Collections.unmodifiableList(this.invitations) : Collections.emptyList();
   }

    public Server withInvitations(Invitation value)
   {
      if (this.invitations == null)
      {
         this.invitations = new ArrayList<>();
      }
      if (!this.invitations.contains(value))
      {
         this.invitations.add(value);
         value.setServer(this);
         this.firePropertyChange(PROPERTY_INVITATIONS, null, value);
      }
      return this;
   }

    public Server withInvitations(Invitation... value)
   {
      for (final Invitation item : value)
      {
         this.withInvitations(item);
      }
      return this;
   }

    public Server withInvitations(Collection<? extends Invitation> value)
   {
      for (final Invitation item : value)
      {
         this.withInvitations(item);
      }
      return this;
   }

    public Server withoutInvitations(Invitation value)
   {
      if (this.invitations != null && this.invitations.remove(value))
      {
         value.setServer(null);
         this.firePropertyChange(PROPERTY_INVITATIONS, value, null);
      }
      return this;
   }

    public Server withoutInvitations(Invitation... value)
   {
      for (final Invitation item : value)
      {
         this.withoutInvitations(item);
      }
      return this;
   }

    public Server withoutInvitations(Collection<? extends Invitation> value)
   {
      for (final Invitation item : value)
      {
         this.withoutInvitations(item);
      }
      return this;
   }

    public String getReferenceMessage()
   {
      return this.referenceMessage;
   }

    public Server setReferenceMessage(String value)
   {
      if (Objects.equals(value, this.referenceMessage))
      {
         return this;
      }

      final String oldValue = this.referenceMessage;
      this.referenceMessage = value;
      this.firePropertyChange(PROPERTY_REFERENCE_MESSAGE, oldValue, value);
      return this;
   }

    public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

    public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

    @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getName());
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getOwner());
      result.append(' ').append(this.getReferenceMessage());
      return result.substring(1);
   }

    public void removeYou()
   {
      this.withoutCategories(new ArrayList<>(this.getCategories()));
      this.withoutInvitations(new ArrayList<>(this.getInvitations()));
      this.withoutMembers(new ArrayList<>(this.getMembers()));
      this.setLocalUser(null);
   }
}
