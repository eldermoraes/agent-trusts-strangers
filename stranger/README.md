# The stranger

The Stranger never talks to the agent. They fill in the public ticket form like any
customer would. Each script opens one ticket; the text of that ticket is the
whole attack. Run them in order as the talk adds one defense after another.

| Script | Used against | What the ticket asks the agent to do |
|---|---|---|
| `hidden-note.sh` | no defense, input guardrail, and the scoped tools alone | e-mail the Victim's profile to the Stranger |
| `close-tag.sh` | instruction/data separation | same, after "closing" the untrusted-content tag |
| `reply-in-ticket.sh` | least privilege, tool-call guardrail | post the Victim's profile as a reply on the Stranger's own ticket |
| `image-beacon.sh` | tool-call guardrail, output guardrail + CSP | put the Victim's voucher in an image URL in the final answer |
| `reset.sh` | any | one honest ticket (the Victim's), all logs cleared |

Every script is a `curl` against `POST http://localhost:8081/tickets`. Open the
files: the payloads are meant to be read on stage.
