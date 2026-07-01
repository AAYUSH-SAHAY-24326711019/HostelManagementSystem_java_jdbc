================================================================
  GIRLS HOSTEL MANAGEMENT SYSTEM
  Final Year MCA Project
  Tech Stack: Java 21, JDBC, Swing GUI, Socket Programming,
              PDFBox 1.8, MySQL 8+
================================================================

QUICK START (Eclipse)
─────────────────────
1. Open Eclipse → File → Import → General → Existing Projects
   into Workspace → browse to this folder → Finish.

2. Add both JARs to build path:
   Right-click project → Build Path → Add External Archives
     • lib/mysql-connector-java-8.0.30.jar
     • lib/pdfbox-1.8.16.jar

3. Import the database schema:
   Open MySQL Workbench → run sql/hostel_management.sql

4. Edit db.properties (in project root) with your MySQL details:
     db.host=localhost
     db.port=3306
     db.name=hostel_management
     db.user=root
     db.password=<your password>

5. Run → Run As → Java Application
   Main class: com.hostel.HostelApp

6. Default admin login:
     Username : admin
     Password : Admin@123
   (Change it immediately from Settings → Change Password)

================================================================
PROJECT STRUCTURE
================================================================
src/
  com/hostel/
    HostelApp.java              ← SINGLE ENTRY POINT
    dao/                        ← All database operations (JDBC)
    model/                      ← Data model POJOs
    gui/
      common/                   ← Login, ChangePassword, Chat panels
      admin/                    ← Admin dashboard + all panels
      girl/                     ← Student portal
      parent/                   ← Parent/guardian portal
    socket/                     ← ChatServer + ChatClient (TCP sockets)
    report/                     ← PDF report generation (PDFBox)
    util/                       ← DB conn, passwords, validation, theme

sql/
  hostel_management.sql         ← Full MySQL schema (24 tables) + seed data

lib/
  mysql-connector-java-8.0.30.jar
  pdfbox-1.8.16.jar

reports/                        ← Generated PDFs saved here (auto-created)

================================================================
MODULES & FEATURES
================================================================
ADMIN MODULE
  ✓ Splash screen + DB connection check on startup
  ✓ Login / Logout / Change password (salted SHA-256 hash)
  ✓ Hostel Control Panel (rename hostel from GUI)
  ✓ Admission form (student + parents + fee structure in one form)
  ✓ View/edit student list (active / left / all)
  ✓ Mark student as Left
  ✓ Generate student & parent login credentials (temp password)
  ✓ Bill generation (Monthly Stay / Deposits / Kitchen / Fine / Other)
  ✓ Record payments; auto-update bill status + dues
  ✓ View all unpaid bills, pending dues
  ✓ Impose / manage fines
  ✓ Notice board (to specific girl / all girls / specific parent / all parents)
  ✓ Canteen service tracker (Rendered / Not Rendered)
  ✓ Auto-generate canteen refunds for Not-Rendered days
  ✓ Pending refund list
  ✓ Complaint management (view / update status / admin remarks)
  ✓ Chat SERVER (TCP socket) + admin chat client embedded
  ✓ PDF reports: active girls, left girls, due-next-month, paid-this-month,
                 all dues, all fines, canteen refunds, student payment history

STUDENT MODULE
  ✓ Login / Logout / Change password (forced on first login)
  ✓ View my profile, room, plan, fee structure, family contacts
  ✓ View all my payments
  ✓ View my pending dues
  ✓ View my fines
  ✓ View canteen service log
  ✓ View notices sent to me
  ✓ File and track complaints
  ✓ Download PDF reports (payments, stay plan, fines)
  ✓ Chat CLIENT (connects to admin's chat server)

PARENT / GUARDIAN MODULE
  ✓ Login with separate parent credentials (linked to girl)
  ✓ Forced password change on first login
  ✓ View ward's profile & fee structure
  ✓ View ward's payments, dues, fines
  ✓ View canteen log, notices addressed to parents
  ✓ Download PDF reports
  ✓ Chat CLIENT

================================================================
DATABASE (24 tables)
================================================================
  hostel_config, admin, room_plans, kitchen_plans, rooms,
  girls, girl_status_history, parents,
  girl_credentials, parent_credentials,
  fee_structure, bills, payments, dues, fines,
  canteen_service_log, canteen_refunds,
  notices, notice_recipients, complaints,
  chat_messages, audit_log,
  password_reset_requests, reports_generated

================================================================
NOTES
================================================================
• Chat server starts automatically when admin logs in (port 5050).
• PDF reports are saved in the reports/ folder next to the project.
• All passwords are salted + 10 000-round SHA-256 hashed — never
  stored in plain text.
• Every critical action is logged to audit_log.
• The app fails safely: DB errors show a friendly dialog instead of
  crashing; logging never propagates exceptions.
================================================================
