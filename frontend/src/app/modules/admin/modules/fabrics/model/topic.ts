export type TopicId = string;

export class FabricTopic {
  readonly id: TopicId;
  readonly name: string;

  private constructor(props: { id: TopicId; name: string }) {
    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { id: TopicId; name: string }): FabricTopic {
    return new FabricTopic(props);
  }
}
