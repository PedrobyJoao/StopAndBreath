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
  - [x] app name is not Stop&Breathe anymore, is Stop And Breathe
  - [ ] UI: better UI app
    - [ ] Be VERY clear about real permissions (pointing out
          line of code where we're setting) and what is asked by android
    - [ ] simple how to give accessibility permission
    - [ ] improve fonts of reminder message (not hardcoded)
    - [ ] image background (same as used for the locker)
    - [ ] feat: Background locker image
  - [x] README (written by me)
    - [x] Document supported android version
    - [x] Be VERY clear about real permissions (pointing out
          line of code where we're setting) and what is asked by android
- [ ] v0.2
  - [ ] feat: idle apps: banks, calculator, clock... (find a better to identify them)
  - [ ] CI/CD: security scan
    - [ ] feat: mention security scan in the app
    - [ ] feat: mention security scan in the README
  - [ ] github: unallow direct commit to main/release branches
  - [ ] github: unallow merge to main without tests+build passing
  - [ ] Icon
  - [ ] CI/CD: publish Google Play when release
  - [ ] CI/CD: publish F-Droid when release
  - [ ] feat(UI-settings): automatic language detection
- [x] License
- [ ] Donation
- [ ] Website for all Stop and Breathe apps
- [ ] v0.3
  - [ ] feat: calendar
  - [ ] feat: google calendar integration
  - [ ] feat: allow users to set their own times for config
        but put a limit of min and max minutes/seconds
  - [ ] feat: reminder levels
  - [ ] UI: choose your language
  - [ ] feat: some different languages: english, pt-br, spanish...
  - [ ] Contributing.md
- [ ] v0.4
  - [ ] feat: maybe allow users to choose if they want strict version
        with accessibility permission or less strict version without accessibility
        permission.
  - [ ] feat: nix flake check and add to CI/CD?
  - [ ] feat: kids version
  - [ ] feat: strictness levels for specific days
  - [ ] feat: EXTREME-STRICT: save database, and keep blocking
