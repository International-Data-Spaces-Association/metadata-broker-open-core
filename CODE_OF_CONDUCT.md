## Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as
contributors and maintainers pledge to making participation in our project and
our community a harassment-free experience for everyone, regardless of age, body
size, disability, ethnicity, gender identity and expression, level of experience,
nationality, personal appearance, race, religion, or sexual identity and
orientation.

### Our Standards

Examples of behavior that contributes to creating a positive environment
include:

* Using welcoming and inclusive language
* Being respectful of differing viewpoints and experiences
* Gracefully accepting constructive criticism
* Focusing on what is best for the community
* Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery and unwelcome sexual attention or
advances
* Trolling, insulting/derogatory comments, and personal or political attacks
* Public or private harassment
* Publishing others' private information, such as a physical or electronic
  address, without explicit permission
* Other conduct which could reasonably be considered inappropriate in a
  professional setting

### Our Responsibilities

Project maintainers are responsible for clarifying the standards of acceptable
behavior and are expected to take appropriate and fair corrective action in
response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or
reject comments, commits, code, wiki edits, issues, and other contributions
that are not aligned to this Code of Conduct, or to ban temporarily or
permanently any contributor for other behaviors that they deem inappropriate,
threatening, offensive, or harmful.

### Scope

This Code of Conduct applies both within project spaces and in public spaces
when an individual is representing the project or its community. Examples of
representing a project or community include using an official project e-mail
address, posting via an official social media account, or acting as an appointed
representative at an online or offline event. Representation of a project may be
further defined and clarified by project maintainers.

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be
reported by contacting the project team. All
complaints will be reviewed and investigated and will result in a response that
is deemed necessary and appropriate to the circumstances. The project team is
obligated to maintain confidentiality with regard to the reporter of an incident.
Further details of specific enforcement policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good
faith may face temporary or permanent repercussions as determined by other
members of the project's leadership.

### Release Principles

We recommend the following release principles:

*	Must haves:
  * Every update comes into its branch with a test or note explaining how it can be tested:
    * Having a branch for each feature/update keeps a better overview of what is currently going on, what has been  merged, et cetera.
  *	Relevant changes are documented in the changelog:
    * Documenting changes in the changelog is essential for users to keep an overview of what is going on and helps with bug fixing after switching to a newer version.
	* After merging new changes into the main branch, final integration tests should be performed to ensure interoperability with other components:
    * Final integration tests are an absolute must-have in order to keep live systems running smoothly.
  *	Only developing into own repositories:
    * We recommend avoiding developing into customer repositories. You will lose your overview of where a feature is implemented or not.
*	Should haves:
  * Every component should have its owner and its release cycle:
    * An Owner is the person who is in charge of testing new features and putting them in place.
  * Using multiple Repositories/Forks:
    * In order to ensure a stable and clean running version, we recommend having 2–3 repositories for each component.
  * Clean up repositories after merging or not merging branches:
    * A feature branch should be deleted after it is merged into the main branch. The same applies to branches with a rejected pull request.
*	Could haves:
	* Delivering only releases to customers:
    * Bug fixing on the customer's end should be avoided.

### Attribution

This Code of Conduct is adapted from the [Contributor Covenant](http://contributor-covenant.org), 
version 1.4, available at http://contributor-covenant.org/version/1/4.
