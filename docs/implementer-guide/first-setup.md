# First Setup

After deploying CareLoom, follow these steps to get your program running.

## Step 1: Log In to the Admin Dashboard

1. Open your browser and go to the admin dashboard URL (e.g., `http://localhost:3000`).
2. Log in using the credentials provided by your deployment operator. For a new local evaluation instance, use the account explicitly created through the [bootstrap setup](../getting-started/self-hosted-deploy.md#start-an-isolated-instance).
3. Confirm the operator has disabled first-install bootstrap after account creation. Do not assume `admin` / `admin` works on a fresh database. Keep evaluation credentials out of an operational deployment.

## Step 2: Create a Project

Projects are the top-level container in CareLoom. Each project groups together a set of curricula, CHWs, and families. You might create one project per study site, region, or program.

1. From the admin dashboard, click **Projects** in the left sidebar.
2. Click **Create Project**.
3. Enter a project name (e.g., "Yunnan Province Pilot") and an optional description.
4. Click **Save**.

Creating a project displays generated project-admin credentials. Store them privately; do not include them in screenshots, issues or shared logs. Sign out of the super-admin account and sign in as that project admin to see the project's curriculum and account menus. The super-admin view and project-admin view have different navigation.

Recovery: If the project-admin credentials are missing or the expected menu is unavailable, ask the deployment operator to verify the account and role rather than creating duplicate projects.

## Step 3: Create Admin Accounts

Use the generated project-admin account to configure the program. Create role-appropriate accounts for program staff through that project's account-management view; menu names may differ with the selected language. Keep each person's login separate.

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

A curriculum defines the structured content that CHWs deliver during home visits. The downloadable Healthy Future PDF is for reading and adaptation under its stated license. It does not populate CareLoom automatically. Create the sessions as modules and components in the admin dashboard, then review the published content on a test device.

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

1. Obtaining an APK built by your technical team for your own deployment. Confirm the configured API endpoint and build identifier before installation.
2. Sharing it with CHWs via direct file transfer, a shared drive, or sideloading.
3. Having each CHW install the APK and sign in with their credentials.

Before field use, sign in online and prepare the assigned curriculum and family data on each device. Test the offline tasks you need with synthetic records, then reconnect and confirm they appear on the server. Keep the same account until pending work has synced. If you sign out with pending work, return to the original account and backend to sync it; ask your operator to review any ownership/recovery hold. Offline caching does not mean every setup or editing action works without connectivity. See [mobile offline behavior](../developer-guide/mobile-app.md#offline-support).

## What's Next

- [Curriculum Management](curriculum-management.md) -- detailed instructions for building visit content
- [Managing CHWs](managing-chws.md) -- assigning supervisors, monitoring activity
- [Security](security.md) -- changing passwords, HTTPS, and backups
