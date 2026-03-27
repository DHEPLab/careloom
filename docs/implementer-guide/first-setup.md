# First Setup

After deploying CareLoom, follow these steps to get your program running.

## Step 1: Log In to the Admin Dashboard

1. Open your browser and go to the admin dashboard URL (e.g., `http://localhost:3000`).
2. Log in with the default credentials:
   - **Username:** `admin`
   - **Password:** `admin`
3. **Change the default password immediately.** Go to the profile area in the top-right corner and select "Change Password."

## Step 2: Create a Project

Projects are the top-level container in CareLoom. Each project groups together a set of curricula, CHWs, and families. You might create one project per study site, region, or program.

1. From the admin dashboard, click **Projects** in the left sidebar.
2. Click **Create Project**.
3. Enter a project name (e.g., "Yunnan Province Pilot") and an optional description.
4. Click **Save**.

All curricula, users, and families you create will be assigned to a project.

## Step 3: Create Admin Accounts

By default, CareLoom has one super-admin account. You should create separate accounts for each person who will manage the program.

1. Click **Users** in the left sidebar.
2. Click **Create User**.
3. Fill in the required fields:
   - **Username** -- a short login name (e.g., `maria.chen`)
   - **Real Name** -- the person's full name
   - **Password** -- a strong initial password (the user should change it on first login)
   - **Role** -- choose the appropriate role:
     - **Admin** -- can manage curricula, CHWs, and view reports within the assigned project
     - **Supervisor** -- can monitor CHW activity and visit completion
4. Assign the user to a project.
5. Click **Save**.

## Step 4: Create CHW Accounts

Community health workers use the mobile app to conduct home visits. Each CHW needs an account.

1. Click **Users** in the left sidebar.
2. Click **Create User**.
3. Set the role to **CHW**.
4. Enter the CHW's username, real name, and a temporary password.
5. Assign the CHW to the correct project.
6. Click **Save**.

Share the login credentials with each CHW. They will use these to sign in to the mobile app.

For detailed CHW management, see [Managing CHWs](managing-chws.md).

## Step 5: Build Your First Curriculum

A curriculum defines the content that CHWs will deliver during home visits. If you have an existing curriculum to upload, you can build it through the admin dashboard.

1. Click **Curriculum** in the left sidebar.
2. Click **Create Curriculum**.
3. Enter a name and description.
4. Begin adding modules -- each module represents one home visit session.

For detailed instructions on building curricula, see [Curriculum Management](curriculum-management.md).

## Step 6: Register Families

Families (represented as "babies" in the system) are registered through the mobile app by CHWs during their first visit. Each baby record includes:

- The baby's name and identifying information
- Stage: **Unborn** (prenatal) or **Born** (postnatal)
- The primary carer's information
- The assigned CHW

Admins can also view and manage family records from the admin dashboard under **Babies** in the left sidebar.

## Step 7: Distribute the Mobile App

The mobile app is an Android application. Distribute the APK to CHWs by:

1. Obtaining the latest APK build from your technical team.
2. Sharing it with CHWs via direct file transfer, a shared drive, or sideloading.
3. Having each CHW install the APK and sign in with their credentials.

The mobile app works offline. CHWs can conduct visits without an internet connection, and the app will sync data when connectivity is restored.

## What's Next

- [Curriculum Management](curriculum-management.md) -- detailed instructions for building visit content
- [Managing CHWs](managing-chws.md) -- assigning supervisors, monitoring activity
- [Security](security.md) -- changing passwords, HTTPS, and backups
