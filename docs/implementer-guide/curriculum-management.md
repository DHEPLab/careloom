# Curriculum Management

Curricula define the content that community health workers deliver during home visits. This guide explains how to create and manage curricula through the admin dashboard.

## Key Concepts

### Stages

Families are tracked through two stages, and curriculum modules are scheduled accordingly:

- **UNBORN** -- prenatal visits scheduled relative to the expected delivery date
- **BORN** -- postnatal visits scheduled relative to the baby's date of birth

### Modules

A module represents one home visit session. Modules contain the content that a CHW reads, shows, and discusses with a family. Each module includes:

- A **number** (determines display order)
- A **topic** (e.g., nutrition, safety, developmental milestones)
- One or more **components** (the actual content blocks)

### Components

Components are the building blocks within a module. There are four types:

| Component Type | What It Is |
|----------------|------------|
| **Text** | Rich text content displayed to the CHW during the visit. This is the main instructional content. |
| **Media** | Images or other media files embedded in the visit flow. Used for visual aids, diagrams, or photos to show caregivers. |
| **Switch** | Conditional branching based on CHW input. For example, showing different content based on the baby's age or a caregiver's response. |
| **Page Footer** | Navigation and action buttons at the bottom of a content page. Controls how the CHW moves through the visit. |

## Creating a Curriculum

1. In the admin dashboard, click **Curriculum** in the left sidebar.
2. Click **Create Curriculum**.
3. Enter a name (e.g., "Early Childhood Development Program") and an optional description.
4. The curriculum is created in **Draft** status.

## Adding Modules

1. Open the curriculum you just created.
2. Click **Add Module**.
3. Fill in:
   - **Module Number** -- the order in which this module appears
   - **Topic** -- select a topic category
4. Click **Save**.

## Adding Components to a Module

1. Open a module.
2. Click **Add Component**.
3. Choose the component type:
   - **Text** -- enter rich text content using the editor. You can format text, add headings, and create lists.
   - **Media** -- upload an image file. Add a caption if needed.
   - **Switch** -- define the branching condition and the content paths. For example: "Is the baby eating solid food?" with different content for Yes and No answers.
   - **Page Footer** -- configure navigation buttons.
4. Drag and drop components to reorder them within the module.
5. Click **Save**.

## Branching Logic (Switch Components)

Switch components enable adaptive visit content. CareLoom supports two branching contexts:

- **Curriculum Branch (MASTER)** -- the default path through the curriculum
- **Variant Branches** -- alternative content paths triggered by conditions

When building a switch component:
1. Define the condition (e.g., baby stage, caregiver response).
2. Create content for each branch path.
3. Set a default path for when no condition is met.

## Scheduling Visits to Baby Ages

Curricula include a schedule that maps modules to specific baby ages or gestational weeks.

1. Open the curriculum and click the **Schedule** tab.
2. For each module, set:
   - **Applicable Days** -- the age range (in days from birth or from expected delivery date) when this module should be delivered
   - **Stage** -- whether this applies to UNBORN or BORN babies
3. Click **Save**.

The system uses this schedule to automatically generate visit assignments for each family based on the baby's stage and age.

## Publishing a Curriculum

When your curriculum is complete and reviewed:

1. Open the curriculum.
2. Click **Publish**.
3. The system creates immutable snapshots (called "Lessons") of each module. This ensures that visits already in progress are not disrupted if you later edit the curriculum.

Once published, the curriculum becomes available to CHWs through the mobile app. You can continue to edit the draft version and re-publish when updates are ready.

## Editing a Published Curriculum

1. Edits are always made to the draft version.
2. In-progress visits continue using the previously published lesson snapshots.
3. When you publish again, new visits will use the updated content.
4. Already-completed or in-progress visits are not affected by re-publishing.

## Questionnaires

Questionnaires are survey instruments attached to a curriculum. They can be used for data collection during visits.

1. Go to the **Surveys** page from the left sidebar.
2. Click **Create Questionnaire**.
3. Add questions and configure any branching logic.
4. Link the questionnaire to a curriculum module.

CHWs complete questionnaires as part of the visit flow, and responses are saved with the visit record.

## Tips

- **Start simple.** Create a curriculum with a few modules first, test it with a small group of CHWs, then expand.
- **Use branching sparingly.** Too many branch paths make curricula hard to maintain and test.
- **Review before publishing.** Have multiple team members review curriculum content in the dashboard before publishing to CHWs.
- **Keep media files small.** CHWs may have limited bandwidth. Compress images before uploading.
