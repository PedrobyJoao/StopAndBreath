Simply TODOs:

- [ ] v0.1
  - [x] initial complete version
  - [x] message defaults
  - [x] timer defaults
  - [x] feat: improve idle time (it's not simple screen on/off,
        because user might be off for a long time).
  - [x] feat: actually, when the reminder comes, it's ok if the user
        turn its screen off because it means they're actually resting
        and doing something else. So we should continue the reminder
        countdown.
  - [x] tests: review if more useful tests shall be added
  - [x] CI/CD: tests + build
  - [x] CI/CD: release
  - [x] feat: idle when using maps apps!
  - [x] app name is not Stop&Breathe anymore, is StopAndBreathe
  - [x] UI: better UI app
    - [x] Be VERY clear about real permissions (pointing out
          line of code where we're setting) and what is asked by android
    - [x] simple how to give accessibility permission
    - [x] background color
    - [x] improve fonts of reminder message (not hardcoded)
    - [x] feat: features
  - [x] README (written by me)
    - [x] Document supported android version
    - [x] Be VERY clear about real permissions (pointing out
          line of code where we're setting) and what is asked by android
    - [x] prod APK
    - [ ] UI: encourage user to do the breathing
    - [x] review and update AGENTS.md with new files and configs

- [ ] v0.2
  - [ ] UI: get version from config
  - [ ] feat: reminder levels
  - [ ] feat: notification before reminder
  - [ ] feat: postpone
  - [ ] feat: debug version with shorter defaults
  - [ ] feat: idle apps: banks, calculator, clock... (find a better to identify them)
  - [ ] CI/CD: publish prod build instead of debug
  - [ ] CI/CD: security scan
    - [ ] feat: mention security scan in the app
    - [ ] feat: mention security scan in the README
  - [ ] Icon
  - [ ] UI review
  - [ ] feat(UI-settings): automatic language detection
  - [ ] CI/CD: publish Google Play when release
  - [ ] CI/CD: publish F-Droid when release
  - [ ] github: unallow direct commit to main/release branches
  - [ ] github: unallow merge to main without tests+build passing

- [x] License
- [ ] Donation
- [ ] Website for all Stop and Breathe apps

- [ ] v0.3
  - [ ] feat: calendar
  - [ ] feat: allow users to set their own times for config
        but put a limit of min and max minutes/seconds
  - [ ] UI: choose your language
  - [ ] feat: some different languages: english, pt-br, spanish...
  - [ ] Contributing.md

- [ ] v0.4
  - [ ] feat: strictness levels for specific days
  - [ ] feat: EXTREME-STRICT option: save database, and keep blocking
  - [ ] feat: maybe allow users to choose if they want strict version
        with accessibility permission or less strict version without accessibility
        permission.

- [ ] v0.5:
  - [ ] feat: google calendar integration
  - [ ] feat: nix flake check and add to CI/CD?
  - [ ] feat: kids version
  - [ ] feat: don't run CICD when only changing markdown files
