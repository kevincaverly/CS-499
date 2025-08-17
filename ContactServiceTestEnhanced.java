import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.Collectors;

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

        // Setter methods for updates
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        // Enhanced: Override toString for better debugging and logging
        @Override
        public String toString() {
            return String.format("Contact{ID='%s', firstName='%s', lastName='%s', phone='%s', address='%s'}", 
                               contactID, firstName, lastName, phoneNumber, address);
        }

        // Enhanced: Override equals and hashCode for proper collection behavior
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Contact contact = (Contact) obj;
            return Objects.equals(contactID, contact.contactID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contactID);
        }
    }

    // Enhanced ContactService with improved algorithms and multiple indexes for fast searching
    public static class ContactService {
        // Main storage: HashMap for O(1) contact lookup by ID
        private Map<String, Contact> contacts;
        
        // Enhanced: Multiple indexes for fast searching
        private Map<String, List<String>> firstNameIndex;
        private Map<String, List<String>> lastNameIndex;    
        private Map<String, String> phoneIndex;           

        // Constructor to initialize contacts map and all search indexes
        public ContactService() {
            contacts = new HashMap<>();
            firstNameIndex = new HashMap<>();
            lastNameIndex = new HashMap<>();
            phoneIndex = new HashMap<>();
        }

        // Enhanced: Add contact with validation and automatic index maintenance
        public boolean addContact(Contact contact) {
            // Input validation: ensure contact and ID are not null/empty
            if (contact == null || contact.getContactID() == null || contact.getContactID().trim().isEmpty()) {
                return false;
            }

            String contactID = contact.getContactID();
            
            // Enhanced: Prevent duplicate contacts by checking if ID already exists
            if (contacts.containsKey(contactID)) {
                return false;
            }

            // Enhanced: Ensure phone number is unique
            if (contact.getPhoneNumber() != null && phoneIndex.containsKey(contact.getPhoneNumber())) {
                return false;
            }

            // Add to main contacts map
            contacts.put(contactID, contact);

            // Enhanced: Automatically update all search indexes for future lookups
            updateIndices(contact, true);

            return true; 
        }

        // Enhanced: Delete contact with proper index cleanup
        public boolean deleteContact(String contactID) {
            // Input validation
            if (contactID == null || contactID.trim().isEmpty()) {
                return false;
            }

            Contact contact = contacts.get(contactID);
            if (contact == null) {
                return false;
            }

            contacts.remove(contactID);

            updateIndices(contact, false);

            return true;
        }

        // Update methods with validation
        public boolean updateFirstName(String contactID, String newFirstName) {
            // Input validation
            if (contactID == null || newFirstName == null || newFirstName.trim().isEmpty()) {
                return false;
            }

            Contact contact = contacts.get(contactID);
            if (contact == null) {
                return false;
            }

            // Update contact and maintain index
            removeFromIndex(firstNameIndex, contact.getFirstName(), contactID);
            contact.setFirstName(newFirstName);
            addToIndex(firstNameIndex, newFirstName, contactID);

            return true;
        }

        public boolean updateLastName(String contactID, String newLastName) {
            // Input validation
            if (contactID == null || newLastName == null || newLastName.trim().isEmpty()) {
                return false;
            }

            Contact contact = contacts.get(contactID);
            if (contact == null) {
                return false;
            }

            // Update contact and maintain index
            removeFromIndex(lastNameIndex, contact.getLastName(), contactID);
            contact.setLastName(newLastName);
            addToIndex(lastNameIndex, newLastName, contactID);

            return true;
        }
        
        public boolean updatePhoneNum(String contactID, String newPhoneNum) {
            // Input validation
            if (contactID == null || newPhoneNum == null || newPhoneNum.trim().isEmpty()) {
                return false;
            }

            Contact contact = contacts.get(contactID);
            if (contact == null) {
                return false;
            }

            // Ensure phone number is unique
            if (phoneIndex.containsKey(newPhoneNum) && !newPhoneNum.equals(contact.getPhoneNumber())) {
                return false;
            }

            // Update contact and maintain phone index
            phoneIndex.remove(contact.getPhoneNumber());
            contact.setPhoneNumber(newPhoneNum);
            phoneIndex.put(newPhoneNum, contactID);

            return true;
        }

        public boolean updateAddress(String contactID, String newAddress) {
            // Input validation
            if (contactID == null || newAddress == null || newAddress.trim().isEmpty()) {
                return false;
            }

            Contact contact = contacts.get(contactID);
            if (contact == null) {
                return false;
            }

            contact.setAddress(newAddress);
            return true;
        }

        // Basic contact retrieval by ID
        public Contact getContact(String contactID) {
            return contacts.get(contactID);
        }

        // Fast search by first name using index
        public List<Contact> searchByFirstName(String firstName) {
            return searchByNameIndex(firstNameIndex, firstName);
        }

        // Fast search by last name using index
        public List<Contact> searchByLastName(String lastName) {
            return searchByNameIndex(lastNameIndex, lastName);
        }

        // Helper method to simplify/consolidate name searching with improved normalization
        private List<Contact> searchByNameIndex(Map<String, List<String>> nameIndex, String name) {
            if (name == null || name.trim().isEmpty()) {
                return new ArrayList<>();
            }

            // Enhanced: Better string normalization - handle internal whitespace and case
            String normalizedName = normalizeString(name);
            List<String> contactIDs = nameIndex.get(normalizedName);
            
            if (contactIDs == null) {
                return new ArrayList<>();
            }

            return contactIDs.stream()
                           .map(contacts::get)
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());
        }

        // Enhanced: Fast search by phone number using index with normalization
        public Contact searchByPhoneNumber(String phoneNumber) {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return null;
            }

            // Enhanced: Normalize phone number for consistent matching
            String normalizedPhone = normalizePhoneNumber(phoneNumber);
            String contactID = phoneIndex.get(normalizedPhone);
            return contactID != null ? contacts.get(contactID) : null;
        }

        // Enhanced: Search by address (new feature)
        public List<Contact> searchByAddress(String address) {
            if (address == null || address.trim().isEmpty()) {
                return new ArrayList<>();
            }

            String normalizedAddress = normalizeString(address);
            return contacts.values().stream()
                         .filter(contact -> contact.getAddress() != null && 
                                          normalizeString(contact.getAddress()).contains(normalizedAddress))
                         .collect(Collectors.toList());
        }

        // Enhanced: Partial name search (new feature)
        public List<Contact> searchByNamePartial(String searchTerm) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return new ArrayList<>();
            }

            String normalizedTerm = normalizeString(searchTerm);
            Set<Contact> results = new HashSet<>();

            // Search in first names
            firstNameIndex.forEach((name, contactIDs) -> {
                if (name.contains(normalizedTerm)) {
                    contactIDs.forEach(id -> {
                        Contact contact = contacts.get(id);
                        if (contact != null) results.add(contact);
                    });
                }
            });

            // Search in last names
            lastNameIndex.forEach((name, contactIDs) -> {
                if (name.contains(normalizedTerm)) {
                    contactIDs.forEach(id -> {
                        Contact contact = contacts.get(id);
                        if (contact != null) results.add(contact);
                    });
                }
            });

            return new ArrayList<>(results);
        }

        // Helper: Normalize strings for consistent searching
        private String normalizeString(String input) {
            if (input == null) return "";
            // Remove extra whitespace, convert to lowercase, trim
            return input.replaceAll("\\s+", " ").toLowerCase().trim();
        }

        // Helper: Normalize phone numbers for consistent matching
        private String normalizePhoneNumber(String phone) {
            if (phone == null) return "";
            // Remove all non-digit characters and trim
            return phone.replaceAll("[^0-9]", "").trim();
        }


        // Enhanced: Get all contacts as a list
        public List<Contact> getAllContacts() {
            return new ArrayList<>(contacts.values());
        }

        // Enhanced: Get total contact count
        public int getContactCount() {
            return contacts.size();
        }

        // Helper methods for index management with improved normalization
        private void updateIndices(Contact contact, boolean isAdding) {
            if (isAdding) {
                // Enhanced: Add contact to all indexes with normalized keys
                addToIndex(firstNameIndex, normalizeString(contact.getFirstName()), contact.getContactID());
                addToIndex(lastNameIndex, normalizeString(contact.getLastName()), contact.getContactID());
                if (contact.getPhoneNumber() != null) {
                    phoneIndex.put(normalizePhoneNumber(contact.getPhoneNumber()), contact.getContactID());
                }
            } else {
                // Enhanced: Remove contact from all indexes with normalized keys
                removeFromIndex(firstNameIndex, normalizeString(contact.getFirstName()), contact.getContactID());
                removeFromIndex(lastNameIndex, normalizeString(contact.getLastName()), contact.getContactID());
                phoneIndex.remove(normalizePhoneNumber(contact.getPhoneNumber()));
            }
        }

        // Helper: Add contact ID to an index with normalization
        private void addToIndex(Map<String, List<String>> index, String key, String contactID) {
            if (key == null || key.trim().isEmpty()) return;
            
            String normalizedKey = normalizeString(key);
            index.computeIfAbsent(normalizedKey, k -> new ArrayList<>()).add(contactID);
        }

        // Helper: Remove contact ID from an index with normalization
        private void removeFromIndex(Map<String, List<String>> index, String key, String contactID) {
            if (key == null || key.trim().isEmpty()) return;
            
            String normalizedKey = normalizeString(key);
            List<String> contactIDs = index.get(normalizedKey);
            if (contactIDs != null) {
                contactIDs.remove(contactID);
                // Clean up empty lists to save memory
                if (contactIDs.isEmpty()) {
                    index.remove(normalizedKey);
                }
            }
        }
    }

    /**
     * Enhanced Test Cases for ContactService
     */
    @Test
    public void testAddContact() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        
        assertTrue(contactService.addContact(contact));
        assertEquals(contact, contactService.getContact("1111111111"));
    }

    @Test
    public void testAddDuplicateContact() {
        ContactService contactService = new ContactService();
        Contact contact1 = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        Contact contact2 = new Contact("1111111111", "John", "Doe", "0987654321", "456 Oak St");
        
        assertTrue(contactService.addContact(contact1));
        assertFalse(contactService.addContact(contact2)); // Should fail - duplicate ID
    }

    @Test
    public void testAddContactWithDuplicatePhone() {
        ContactService contactService = new ContactService();
        Contact contact1 = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        Contact contact2 = new Contact("2222222222", "John", "Doe", "1234567890", "456 Oak St");
        
        assertTrue(contactService.addContact(contact1));
        assertFalse(contactService.addContact(contact2)); // Should fail - duplicate phone
    }

    @Test
    public void testDeleteContact() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        assertTrue(contactService.deleteContact("1111111111"));
        assertNull(contactService.getContact("1111111111"));
    }

    @Test
    public void testDeleteNonExistentContact() {
        ContactService contactService = new ContactService();
        assertFalse(contactService.deleteContact("9999999999"));
    }

    @Test
    public void testUpdateFirstName() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        assertTrue(contactService.updateFirstName("1111111111", "John"));
        assertEquals("John", contactService.getContact("1111111111").getFirstName());
    }

    @Test
    public void testUpdateLastName() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        assertTrue(contactService.updateLastName("1111111111", "Smith"));
        assertEquals("Smith", contactService.getContact("1111111111").getLastName());
    }

    @Test
    public void testUpdatePhoneNum() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        assertTrue(contactService.updatePhoneNum("1111111111", "0987654321"));
        assertEquals("0987654321", contactService.getContact("1111111111").getPhoneNumber());
    }

    @Test
    public void testUpdatePhoneNumWithDuplicate() {
        ContactService contactService = new ContactService();
        Contact contact1 = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        Contact contact2 = new Contact("2222222222", "John", "Doe", "0987654321", "456 Oak St");
        contactService.addContact(contact1);
        contactService.addContact(contact2);
        
        assertFalse(contactService.updatePhoneNum("1111111111", "0987654321")); // Should fail - duplicate phone
    }

    @Test
    public void testUpdateAddress() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        assertTrue(contactService.updateAddress("1111111111", "321 First St"));
        assertEquals("321 First St", contactService.getContact("1111111111").getAddress());
    }

    @Test
    public void testSearchByFirstName() {
        ContactService contactService = new ContactService();
        Contact contact1 = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        Contact contact2 = new Contact("2222222222", "Kevin", "Smith", "0987654321", "456 Oak St");
        Contact contact3 = new Contact("3333333333", "John", "Doe", "5555555555", "789 Pine St");
        
        contactService.addContact(contact1);
        contactService.addContact(contact2);
        contactService.addContact(contact3);
        
        List<Contact> results = contactService.searchByFirstName("Kevin");
        assertEquals(2, results.size());
        assertTrue(results.contains(contact1));
        assertTrue(results.contains(contact2));
    }

    @Test
    public void testSearchByLastName() {
        ContactService contactService = new ContactService();
        Contact contact1 = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        Contact contact2 = new Contact("2222222222", "John", "Caverly", "0987654321", "456 Oak St");
        
        contactService.addContact(contact1);
        contactService.addContact(contact2);
        
        List<Contact> results = contactService.searchByLastName("Caverly");
        assertEquals(2, results.size());
        assertTrue(results.contains(contact1));
        assertTrue(results.contains(contact2));
    }

    @Test
    public void testSearchByPhoneNumber() {
        ContactService contactService = new ContactService();
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        
        Contact result = contactService.searchByPhoneNumber("1234567890");
        assertEquals(contact, result);
    }

    @Test
    public void testGetAllContacts() {
        ContactService contactService = new ContactService();
        Contact contact1 = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        Contact contact2 = new Contact("2222222222", "John", "Smith", "0987654321", "456 Oak St");
        
        contactService.addContact(contact1);
        contactService.addContact(contact2);
        
        List<Contact> allContacts = contactService.getAllContacts();
        assertEquals(2, allContacts.size());
        assertTrue(allContacts.contains(contact1));
        assertTrue(allContacts.contains(contact2));
    }

    @Test
    public void testGetContactCount() {
        ContactService contactService = new ContactService();
        assertEquals(0, contactService.getContactCount());
        
        Contact contact = new Contact("1111111111", "Kevin", "Caverly", "1234567890", "123 Main St");
        contactService.addContact(contact);
        assertEquals(1, contactService.getContactCount());
        
        contactService.deleteContact("1111111111");
        assertEquals(0, contactService.getContactCount());
    }
}