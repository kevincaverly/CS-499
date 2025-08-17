import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

public class ContactServiceTest {
    public static class Contact {
        // Contact information variables
        private String contactID;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;

        // Constructor to initialize contact information
        public Contact(String contactID, String firstName, String lastName, String phoneNumber, String address) {
            this.contactID = contactID;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        // Getter Methods to access contact details

        public String getContactID() {
            return contactID;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getAddress() {
            return address;
        }
    }

    // Class used to manage the contacts
    public static class ContactService {
        private Map<String, Contact> contacts;

        // Constructor to initialize contacts map
        public ContactService() {
            contacts = new HashMap<>();
        }

        // Adds the given contact
        public void addContact(Contact contact) {
            contacts.put(contact.getContactID(), contact);
        }

        // Deletes the given contact
        public void deleteContact(String contactID) {
            contacts.remove(contactID);
        }

        // Updates the first name of the given contact
        public void updateFirstName(String contactID, String newFirstName) {
            Contact contact = contacts.get(contactID);
            if (contact != null) {
                contact.firstName = newFirstName;
            }
        }

        // Updates the last name of the given contact
        public void updateLastName(String contactID, String newLastName) {
            Contact contact = contacts.get(contactID);
            if (contact != null) {
                contact.lastName = newLastName;
            }
        }
        
        // Updates the phone number of the given contact
        public void updatePhoneNum(String contactID, String newPhoneNum) {
            Contact contact = contacts.get(contactID);
            if (contact != null) {
                contact.phoneNumber = newPhoneNum;
            }
        }

        // Updates the address of the given contact
        public void updateAddress(String contactID, String newAddress) {
            Contact contact = contacts.get(contactID);
            if (contact != null) {
                contact.address = newAddress;
            }
        }

        // Returns the contact based on the contactID
        public Contact getContact(String contactID) {
            return contacts.get(contactID);
        }
    }

    /**
     * Various Test Cases for ContactService
     */
    @Test
    public void testAddContact() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        assertEquals(contact, contactService.getContact("1111111111"));
    }

    @Test
    public void testDeleteContact() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        contactService.deleteContact("1111111111");

        assertNull(contactService.getContact("1111111111"));
    }

    @Test
    public void testUpdateFirstName() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        contactService.updateFirstName("1111111111", "John");

        assertEquals("John", contactService.getContact("1111111111").getFirstName());
    }

    @Test
    public void testUpdateLastName() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        contactService.updateLastName("1111111111", "Smith");

        assertEquals("Smith", contactService.getContact("1111111111").getLastName());
    }

    @Test
    public void testUpdatePhoneNum() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        contactService.updatePhoneNum("1111111111", "0987654321");

        assertEquals("1234567890", contactService.getContact("1111111111").getPhoneNumber());
    }

    @Test
    public void testUpdateAddress() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        contactService.updateAddress("1111111111", "321 First St");

        assertEquals("321 First St", contactService.getContact("1111111111").getAddress());
    }
}