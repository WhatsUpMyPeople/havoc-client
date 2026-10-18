package havoc.client.features.module.modules.misc;

import havoc.client.features.gui.diagnostics.FailureText;
import havoc.client.runtime.error.FailureIsolation;

import havoc.client.config.values.BooleanValue;
import havoc.client.config.values.ModeValue;
import havoc.client.events.Event;
import havoc.client.events.EventHandler;
import havoc.client.events.Events;
import havoc.client.features.command.CommandOutput;
import havoc.client.features.gui.chat.actions.ChatMessageActions;
import havoc.client.features.gui.chat.actions.MessageActionRegistry;
import havoc.client.features.gui.clickgui.settings.Setting;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.runtime.error.ErrorHandler;
import havoc.client.runtime.error.FailureRegistry;
import havoc.client.runtime.HavocBuild;
import net.minecraft.client.Minecraft;

/** Failing, resource-free diagnostic fixture. Dev source set only. */
@ModuleInfo(id = "deverrorprobe", displayName = "ErrorHandler Probe", category = ModuleCategory.MISC,
        description = "module.deverrorprobe.description")
public final class DevErrorProbe extends Module {
    private final ModeValue scenario = modeSetting("devErrorScenario", "scenario", "Tick",
            "Tick", "Event", "Setting", "Component", "Enable", "Cleanup", "Recovery");
    private final BooleanValue trigger = bool("devErrorTrigger", "trigger", false);
    private final BooleanValue probeSetting = bool("devErrorSetting", "probe_setting", false);
    private final Object componentKey = new Object();
    private boolean failNextEnable;
    private boolean failNextCleanup;
    private boolean eventArmed;
    private int healthyTicks;
    private final MessageActionRegistry.Registration diagnosticActions;

    public DevErrorProbe() {
        if (!HavocBuild.isDevelopmentBuild())
            throw new IllegalStateException("DevErrorProbe requires a Havoc dev build");
        diagnosticActions = ChatMessageActions.register("havoc.devprobe", new MessageActionRegistry.Provider() {
            private FailureRegistry.Failure resolve(MessageActionRegistry.Context context) {
                if (!"havoc:error".equals(context.type())) return null;
                try { return ErrorHandler.byId(Long.parseLong(context.data("failureId"))); }
                catch (NumberFormatException | NullPointerException e) { return null; }
            }
            @Override public java.util.List<MessageActionRegistry.Action> actions(MessageActionRegistry.Context context) {
                FailureRegistry.Failure f = resolve(context);
                if (f == null || f.scope() != FailureRegistry.Scope.MODULE || !name().equals(f.component()))
                    return java.util.List.of();
                return java.util.List.of(new MessageActionRegistry.Action("reset", FailureText.tr("dev.action.reset"),
                        MessageActionRegistry.Tone.DANGER, ErrorHandler.failure(DevErrorProbe.this) != null));
            }
            @Override public void execute(MessageActionRegistry.Context context, String actionId) {
                FailureRegistry.Failure f = resolve(context);
                if ("reset".equals(actionId) && f != null && name().equals(f.component())
                        && ErrorHandler.failure(DevErrorProbe.this) != null) resetFixture();
            }
        });
    }
    @Override public void onEnable() {
        if (failNextEnable) {
            failNextEnable = false;
            throw new IllegalStateException("DEVELOPMENT TEST: enable/recovery failure");
        }
    }
    @Override public void onDisable() {
        trigger.set(false);
        eventArmed = false;
        if (failNextCleanup) {
            failNextCleanup = false;
            throw new IllegalStateException("DEVELOPMENT TEST: cleanup failure");
        }
    }
    @Override public void onTick() {
        healthyTicks++;
        if (!trigger.get()) return;
        trigger.set(false); // Consume before executing; never an automatic crash loop.
        if ("Tick".equals(scenario.get()))
            throw new IllegalStateException("DEVELOPMENT TEST: module tick failure");
        runScenario(scenario.get());
    }
    @EventHandler public void onProbe(ProbeEvent event) {
        if (eventArmed) {
            eventArmed = false;
            throw new IllegalStateException("DEVELOPMENT TEST: EventBus handler failure");
        }
    }
    public static final class ProbeEvent extends Event { }

    /** Called by the dev-only command or the module's one-shot setting. */
    public void runScenario(String requested) {
        Minecraft mc = Minecraft.getInstance();
        if (!HavocBuild.isDevelopmentBuild() || mc == null || !mc.isSameThread()) return;
        if (requested == null) return;
        switch (requested.toLowerCase(java.util.Locale.ROOT)) {
            case "tick" -> {
                if (!isEnabled()) { CommandOutput.warning(FailureText.tr("dev.enable_first")); return; }
                // The UI trigger is consumed by onTick; command execution routes through the same module boundary.
                scenario.set("Tick");
                trigger.set(true);
            }
            case "event" -> {
                if (!isEnabled()) { CommandOutput.warning(FailureText.tr("dev.enable_first")); return; }
                eventArmed = true;
                Events.BUS.post(new ProbeEvent());
            }
            case "setting" -> {
                Setting setting = getSettings().stream().filter(s -> "probe_setting".equals(s.getId())).findFirst().orElse(null);
                if (setting != null) FailureIsolation.runSetting(setting, "dev-test", () -> {
                    throw new IllegalStateException("DEVELOPMENT TEST: setting callback failure");
                });
            }
            case "component" -> FailureIsolation.reportComponent(componentKey, "DevErrorProbe component", "dev-test",
                    new IllegalStateException("DEVELOPMENT TEST: isolated component failure"));
            case "enable" -> {
                trigger.set(false);
                if (ErrorHandler.blocked(this)) { CommandOutput.warning(FailureText.tr("dev.reset_first")); return; }
                if (isEnabled()) setEnabledManual(false);
                failNextEnable = true;
                setEnabledManual(true);
            }
            case "cleanup" -> {
                if (!isEnabled()) { CommandOutput.warning(FailureText.tr("dev.enable_first")); return; }
                failNextCleanup = true;
                throw new IllegalStateException("DEVELOPMENT TEST: failure followed by failing cleanup");
            }
            case "recovery" -> {
                if (ErrorHandler.failure(this) == null) { CommandOutput.warning(FailureText.tr("dev.fail_first")); return; }
                failNextEnable = true;
                FailureIsolation.retryModule(this);
            }
            default -> CommandOutput.warning(FailureText.tr("dev.unknown_scenario", requested));
        }
    }
    /** A separate entrypoint allows a real tick failure rather than reporting an invented exception. */
    public void armScenario(String name) {
        if (!isEnabled()) { CommandOutput.warning(FailureText.tr("dev.enable_first")); return; }
        scenario.set(name);
        trigger.set(true);
    }
    public void armTick() {
        if (!isEnabled()) { CommandOutput.warning(FailureText.tr("dev.enable_first")); return; }
        scenario.set("Tick");
        trigger.set(true);
    }
    public void resetFixture() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || !mc.isSameThread()) return;
        trigger.set(false);
        failNextEnable = false;
        failNextCleanup = false;
        eventArmed = false;
        // This fixture owns no GPU/native resources. Explicit dev reset is allowed after failed cleanup.
        FailureIsolation.drain();
        if (ErrorHandler.failure(this) != null) {
            if (isLifecycleEnabled()) quarantineAfterFailure();
        } else if (isEnabled()) setEnabledManual(false);
        if (!isLifecycleEnabled()) FailureIsolation.unregister(this);
        for (Setting setting : getSettings()) {
            if (ErrorHandler.failure(setting) != null) FailureIsolation.unregister(setting);
        }
        FailureIsolation.unregister(componentKey);
        CommandOutput.success(FailureText.tr("dev.reset"));
    }
    public void status() {
        FailureRegistry.Failure f = ErrorHandler.failure(this);
        CommandOutput.send(FailureText.tr("dev.status", isEnabled(), healthyTicks,
                f == null ? FailureText.tr("dev.none") : "#" + f.id() + " " + FailureText.state(f.state()),
                FailureIsolation.canRetryModule(this)));
        for (Setting setting : getSettings()) {
            FailureRegistry.Failure sf = ErrorHandler.failure(setting);
            if (sf != null) CommandOutput.warning(FailureText.tr("dev.setting_status", setting.getDisplayName(), sf.id(), FailureText.state(sf.state())));
        }
    }
}
