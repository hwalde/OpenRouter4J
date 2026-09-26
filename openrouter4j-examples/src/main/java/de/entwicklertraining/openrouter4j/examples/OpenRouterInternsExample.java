package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.interns.OpenRouterIntern;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternChatAccumulator;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternDaemonAccessResponse;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternInvokeResponse;
import java.util.List;

/**
 * Demonstrates the interns surface (the OpenRouter "Ori" programme): list,
 * create, lifecycle actions, fire-and-forget invoke, daemon access for
 * {@code ori tui --host} and the streaming chat with the
 * {@code openrouter.provide_input} interaction loop.
 *
 * <p>Traps: every path answers 404 for keys outside the interns programme;
 * the chat endpoint only streams; a paused run waits 5 minutes (answering
 * later is rejected with {@code 409 interaction_not_pending}); closing an
 * active stream cancels the run; the request {@code model} field is never
 * used - change the intern's model via the update endpoint.
 */
public class OpenRouterInternsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Create an intern (idempotent on retry). provision(true) boots it
        // immediately; otherwise it stays queued until provision() is called.
        OpenRouterIntern created = client.interns().create("research-bot")
                .description("Researches customer questions")
                .instructions("Be concise and cite sources.")
                .provision(true)
                .idempotencyKey("create-research-bot-2026-09-16")
                .execute()
                .intern();
        if (created == null) {
            return;
        }
        System.out.println("Created intern " + created.id()
                + " (" + created.name() + "), status " + created.status());

        // First boot of a queued intern, or resume after a suspension.
        client.interns().provision(created.id()).execute();
        // client.interns().suspend(created.id()).execute(); // stop the runtime,
        // keep disk and configuration

        // Start a run without waiting for it (fire-and-forget): answers 202
        // with session_id + status; the run continues on the intern and
        // reports through its own tools (e.g. Slack). Send the session_id
        // back to continue the conversation - it is only accepted from the
        // caller it was issued to, on the same intern.
        OpenRouterInternInvokeResponse invoked =
                client.interns().invoke(created.id(), "Summarize the open pull requests.")
                        .execute();
        System.out.println("Invoke " + invoked.status() + " (session "
                + invoked.sessionId() + ")");

        // Continue the same conversation by sending the session_id back.
        // Trap: the id is only accepted from the caller it was issued to,
        // on the same intern - any other answers 404 (unlike the chat
        // endpoint's session_id, a mistyped one does not fork).
        OpenRouterInternInvokeResponse steered = client.interns()
                .invoke(created.id(), "Follow up on the summary.")
                .sessionId(invoked.sessionId())
                .execute();
        System.out.println("Follow-up " + steered.status()
                + (steered.isSteered() ? " (delivered into the running turn)" : ""));

        // Daemon access for `ori tui --host`: origin + bearer token. The
        // token is a credential (each reveal is logged server-side), and
        // regional hostnames such as eu.openrouter.ai refuse this endpoint.
        OpenRouterInternDaemonAccessResponse daemon =
                client.interns().daemonAccess(created.id()).execute();
        System.out.println("Attach: ori tui --host " + daemon.origin());
        // daemon.token() is the one plaintext reveal - never log it.

        // Stream one turn. The accumulator forwards every raw chunk and gives
        // typed access to the pieces of the interaction loop.
        OpenRouterInternChatAccumulator turn = new OpenRouterInternChatAccumulator(null);
        client.interns().chat(created.id())
                .addUserMessage("Summarize the open pull requests.")
                .approvalMode("manual") // ask before approval-bearing tools run
                .stream(turn)
                .execute();

        System.out.println("Turn finished: " + turn.finishReason());
        System.out.println("Text: " + turn.text());

        if (turn.isInteractionPending()) {
            // The intern paused the run and asked for input: one tool call
            // named openrouter.provide_input, interaction id in toolCall().id().
            OpenRouterInternChatAccumulator.OpenRouterInternToolCall call = turn.toolCall();
            System.out.println("Interaction " + call.id() + ": " + call.arguments());
            // Answer within the 5-minute window, with the session_id from the
            // final chunk. Permission answers are one of the offered options
            // ("allow_once", "allow_always", "reject_once", "reject_always")
            // or "cancel"; question answers are {"action":...} objects.
            OpenRouterInternChatAccumulator answer = new OpenRouterInternChatAccumulator(null);
            client.interns().chat(created.id())
                    .addEchoedAssistantMessage(null, call.id(), call.arguments())
                    .addToolReply(call.id(), "allow_once")
                    .sessionId(turn.sessionId())
                    .stream(answer)
                    .execute();
            System.out.println("After answer: " + answer.finishReason() + " " + answer.text());
        } else if (turn.isFailed()) {
            System.out.println("Streamed failure: " + turn.errorCode() + " "
                    + turn.errorReason() + " (retryable: " + turn.errorRetryable() + ")");
        }

        // Update settings (omitted fields stay unchanged) and read back.
        client.interns().update(created.id()).model("anthropic/claude-sonnet-4.5").execute();
        List<OpenRouterIntern> interns = client.interns().list()
                .status("running", "queued")
                .limit(50)
                .execute()
                .interns();
        interns.forEach(i -> System.out.println("Intern " + i.name() + " is " + i.status()));

        // Safe teardown of intern, runtime and vault (permanent).
        client.interns().delete(created.id()).acknowledgeWorkspaceLoss(true).execute();
    }
}
