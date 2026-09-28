# The stranger

the Stranger never talks to the agent. She fills in the public ticket form like any
customer would. Each script opens one ticket; the text of that ticket is the
whole attack. Run them in order as the talk moves through the rounds.

| Script | Used against | What the ticket asks the agent to do |
|---|---|---|
| `round0.sh` | stage 0 and 1 | e-mail the Victim's profile to the Stranger |
| `round1-close-tag.sh` | stage 2 (fixed delimiter) | same, after "closing" the untrusted-content tag |
| `round2-reply-in-ticket.sh` | stage 3 (least privilege) | post the Victim's profile as a reply on the Stranger's own ticket |
| `round3-image-beacon.sh` | stage 4 (tool guardrail) | put the Victim's voucher in an image URL in the final answer |
| `reset.sh` | any | tickets back to the two honest ones, stranger's log cleared |

Every script is a `curl` against `POST http://localhost:8081/tickets`. Open the
files: the payloads are meant to be read on stage.
