// Default Havoc player-rig graph bootstrap.
(() => {
  const R=globalThis.HavocPlayerRig;
  if (!R || typeof R.PlayerRigMotionGraph!=='function') throw new Error('Havoc player rig modules were not loaded');
  globalThis.HavocPlayerAnimations=R;
  const graph=new R.PlayerRigMotionGraph(playerRig);
  globalThis.__havoc_default_player_rig_graph=graph;
  playerRig.onPose(context=>graph.apply(context));
})();
