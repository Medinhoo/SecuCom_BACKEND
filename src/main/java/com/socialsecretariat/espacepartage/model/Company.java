package com.socialsecretariat.espacepartage.model;
// package com.example.socialsecretariat.model;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import org.hibernate.annotations.Where;

// import java.time.LocalDate;
// import java.util.HashSet;
// import java.util.Set;
// import java.util.UUID;

// @Entity
// @Table(name = "TCompany")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class Company {

// @Id
// @GeneratedValue(strategy = GenerationType.UUID)
// private UUID id;

// @Column(nullable = false)
// private String name;

// @Column(unique = true)
// private String bceNumber;

// @Column(unique = true)
// private String onssNumber;

// private String legalForm;
// private String companyName;

// @Column(name = "creation_date")
// private LocalDate creationDate;

// @Column(unique = true)
// private String vatNumber;

// private String activitySector;
// private String naceCode;
// private String jointCommittee;
// private String holidaySystem;
// private String workCalendar;

// @Column(name = "collaboration_start_date")
// private LocalDate collaborationStartDate;

// private String subscriptionFormula;
// private String declarationFrequency;

// // Relationships
// @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Set<CompanyContact> contacts = new HashSet<>();

// @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Set<CompanyWorker> workers = new HashSet<>();

// @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Set<ServiceRequest> serviceRequests = new HashSet<>();

// @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Set<Dimona> dimonas = new HashSet<>();

// @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Set<Document> documents = new HashSet<>();

// @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval =
// true)
// private Set<ServiceBilling> billings = new HashSet<>();

// @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, orphanRemoval =
// true)
// @JoinColumn(name = "entity_id")
// @Where(clause = "entity_type = 'COMPANY'")
// private Set<Address> addresses = new HashSet<>();

// @ManyToOne
// @JoinColumn(name = "secretariat_employee_id")
// private SecretariatEmployee responsibleEmployee;

// // Helper methods for bidirectional relationships
// public void addContact(CompanyContact contact) {
// contacts.add(contact);
// contact.setCompany(this);
// }

// public void removeContact(CompanyContact contact) {
// contacts.remove(contact);
// contact.setCompany(null);
// }

// public void addWorker(CompanyWorker worker) {
// workers.add(worker);
// worker.setCompany(this);
// }

// public void removeWorker(CompanyWorker worker) {
// workers.remove(worker);
// worker.setCompany(null);
// }

// public void addServiceRequest(ServiceRequest request) {
// serviceRequests.add(request);
// request.setCompany(this);
// }

// public void removeServiceRequest(ServiceRequest request) {
// serviceRequests.remove(request);
// request.setCompany(null);
// }

// public void addDimona(Dimona dimona) {
// dimonas.add(dimona);
// dimona.setCompany(this);
// }

// public void removeDimona(Dimona dimona) {
// dimonas.remove(dimona);
// dimona.setCompany(null);
// }

// public void addDocument(Document document) {
// documents.add(document);
// document.setCompany(this);
// }

// public void removeDocument(Document document) {
// documents.remove(document);
// document.setCompany(null);
// }

// public void addBilling(ServiceBilling billing) {
// billings.add(billing);
// billing.setCompany(this);
// }

// public void removeBilling(ServiceBilling billing) {
// billings.remove(billing);
// billing.setCompany(null);
// }
// }