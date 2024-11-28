import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Easy to Use',
    Svg: require('@site/static/img/undraw_gradle_mountain.svg').default,
    description: (
      <>
        Tableau is designed to be easy to use.
        Start with a simple gradle project and add the plugin.
      </>
    ),
  },
  {
    title: 'Focus on What Matters',
    Svg: require('@site/static/img/undraw_tableau_tree.svg').default,
    description: (
      <>
        Tableau lets you focus on your Mods, and we&apos;ll do the chores. Go
        ahead and apply our plugin, configure the project, and start coding.
      </>
    ),
  },
  {
    title: 'Powered by Gradle',
    Svg: require('@site/static/img/undraw_tableau_gradle.svg').default,
    description: (
      <>
        Extend or customize your project setup by reusing Gradle components.
        Tableau can be extended while reusing the same DSL structure.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
